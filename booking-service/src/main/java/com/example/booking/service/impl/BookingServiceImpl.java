package com.example.booking.service.impl;

import com.example.booking.dto.message.BookingCancelledEvent;
import com.example.booking.dto.message.BookingConfirmedEvent;
import com.example.booking.dto.message.BookingCreatedEvent;
import com.example.booking.dto.message.FareValidationResponse;
import com.example.booking.dto.request.*;
import com.example.booking.dto.response.*;
import com.example.booking.entity.*;
import com.example.booking.exception.BookingNotFoundException;
import com.example.booking.exception.FareValidationException;
import com.example.booking.exception.InvalidBookingStateException;
import com.example.booking.mapper.BookingMapper;
import com.example.booking.messaging.BookingEventPublisher;
import com.example.booking.messaging.FareMessagePublisher;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingStatusHistoryRepository;
import com.example.booking.repository.FlightSeatRepository;
import com.example.booking.service.BookingReferenceGenerator;
import com.example.booking.service.BookingService;
import com.example.booking.service.PnrGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final FlightSeatRepository flightSeatRepository;
    private final BookingMapper bookingMapper;
    private final PnrGenerator pnrGenerator;
    private final BookingReferenceGenerator referenceGenerator;
    private final FareMessagePublisher fareMessagePublisher;
    private final BookingEventPublisher bookingEventPublisher;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, String idempotencyKey) {
        log.info("Initiating booking creation: userId={}, flightId={}, fareId={}, passengers={}, idempotencyKey={}",
                request.getUserId(), request.getFlightId(), request.getFareId(), request.getPassengers().size(), idempotencyKey);

        // 1. Check Idempotency Key
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Booking> existing = bookingRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent request matched existing bookingId={}, pnr={}",
                        existing.get().getId(), existing.get().getPnr());
                return bookingMapper.toResponse(existing.get());
            }
        }

        // 2. Validate Passenger Demographics
        validatePassengers(request.getPassengers());

        // 3. Inter-Service Communication via RabbitMQ Request-Reply
        int passengerCount = request.getPassengers().size();
        FareValidationResponse fareResponse = fareMessagePublisher.requestFareValidation(
                request.getFlightId(),
                request.getFareId(),
                passengerCount
        );

        if (fareResponse == null || Boolean.FALSE.equals(fareResponse.getValid())) {
            String msg = (fareResponse != null && fareResponse.getMessage() != null)
                    ? fareResponse.getMessage()
                    : "Fare validation failed for flight " + request.getFlightId() + " and fare " + request.getFareId();
            log.warn("Fare validation rejected: {}", msg);
            throw new FareValidationException(msg);
        }

        // 4. Generate Airline Standard PNR and Booking Reference
        String pnr = pnrGenerator.generatePnr();
        String bookingRef = referenceGenerator.generateReference();

        // 5. Build Booking Entity with Member Differential Pricing
        Booking booking = Booking.builder()
                .bookingReference(bookingRef)
                .pnr(pnr)
                .userId(request.getUserId())
                .flightId(request.getFlightId())
                .fareId(request.getFareId())
                .fareCode(fareResponse.getFareCode())
                .fareType(fareResponse.getFareType())
                .cabinClass(fareResponse.getCabinClass())
                .currency(fareResponse.getCurrency() != null ? fareResponse.getCurrency() : "INR")
                .bookingStatus(BookingStatus.PAYMENT_PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();

        // 6. Calculate Member-Type Pricing and map passengers
        applyMemberPricingAndPassengers(booking, fareResponse, request.getPassengers());

        Booking savedBooking = bookingRepository.save(booking);

        // 7. Assign Seats as RESERVED
        assignSeats(savedBooking, request.getPassengers(), SeatStatus.RESERVED, null);

        // 8. Record Status History Audit Trail
        saveStatusHistory(savedBooking.getId(), null, BookingStatus.INITIATED, "Booking created via client request");
        saveStatusHistory(savedBooking.getId(), BookingStatus.INITIATED, BookingStatus.PAYMENT_PENDING, "Fare validated; awaiting payment processing");

        // 9. Publish booking.created Event via RabbitMQ
        publishCreatedEvent(savedBooking, passengerCount);

        log.info("Booking created successfully: id={}, reference={}, pnr={}, totalAmount={}",
                savedBooking.getId(), savedBooking.getBookingReference(), savedBooking.getPnr(), savedBooking.getTotalAmount());

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse holdBooking(HoldBookingRequest request) {
        log.info("Initiating booking hold: userId={}, flightId={}, fareId={}, passengers={}, holdDuration={}",
                request.getUserId(), request.getFlightId(), request.getFareId(), request.getPassengers().size(), request.getHoldDurationMinutes());

        validatePassengers(request.getPassengers());

        int passengerCount = request.getPassengers().size();
        FareValidationResponse fareResponse = fareMessagePublisher.requestFareValidation(
                request.getFlightId(),
                request.getFareId(),
                passengerCount
        );

        if (fareResponse == null || Boolean.FALSE.equals(fareResponse.getValid())) {
            String msg = (fareResponse != null && fareResponse.getMessage() != null)
                    ? fareResponse.getMessage()
                    : "Fare validation failed for flight " + request.getFlightId() + " and fare " + request.getFareId();
            throw new FareValidationException(msg);
        }

        String pnr = pnrGenerator.generatePnr();
        String bookingRef = referenceGenerator.generateReference();
        int holdMinutes = (request.getHoldDurationMinutes() != null && request.getHoldDurationMinutes() > 0)
                ? request.getHoldDurationMinutes()
                : 15;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(holdMinutes);

        Booking booking = Booking.builder()
                .bookingReference(bookingRef)
                .pnr(pnr)
                .userId(request.getUserId())
                .flightId(request.getFlightId())
                .fareId(request.getFareId())
                .fareCode(fareResponse.getFareCode())
                .fareType(fareResponse.getFareType())
                .cabinClass(fareResponse.getCabinClass())
                .currency(fareResponse.getCurrency() != null ? fareResponse.getCurrency() : "INR")
                .bookingStatus(BookingStatus.HELD)
                .paymentStatus(PaymentStatus.PENDING)
                .holdExpiresAt(expiresAt)
                .build();

        applyMemberPricingAndPassengers(booking, fareResponse, request.getPassengers());

        Booking savedBooking = bookingRepository.save(booking);

        // Assign Seats as HELD with expiration
        assignSeats(savedBooking, request.getPassengers(), SeatStatus.HELD, expiresAt);

        saveStatusHistory(savedBooking.getId(), null, BookingStatus.INITIATED, "Hold request initiated");
        saveStatusHistory(savedBooking.getId(), BookingStatus.INITIATED, BookingStatus.HELD, "Booking placed on temporary hold for " + holdMinutes + " minutes");

        publishCreatedEvent(savedBooking, passengerCount);

        log.info("Booking held successfully: id={}, reference={}, pnr={}, holdExpiresAt={}",
                savedBooking.getId(), savedBooking.getBookingReference(), savedBooking.getPnr(), savedBooking.getHoldExpiresAt());

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse releaseBookingHold(Long bookingId) {
        log.info("Releasing hold for bookingId={}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getBookingStatus() != BookingStatus.HELD) {
            throw new InvalidBookingStateException("Booking is not currently in HELD status. Current status: " + booking.getBookingStatus());
        }

        releaseSeatsForBooking(bookingId);

        BookingStatus previous = booking.getBookingStatus();
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason("Hold released by customer");
        Booking saved = bookingRepository.save(booking);

        saveStatusHistory(saved.getId(), previous, BookingStatus.CANCELLED, "Hold released by customer");

        BookingCancelledEvent cancelledEvent = BookingCancelledEvent.builder()
                .eventId("EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .bookingId(saved.getId())
                .pnr(saved.getPnr())
                .bookingReference(saved.getBookingReference())
                .originalAmount(saved.getTotalAmount())
                .cancellationFee(BigDecimal.ZERO)
                .refundAmount(BigDecimal.ZERO)
                .currency(saved.getCurrency())
                .reason("Hold released by customer")
                .timestamp(LocalDateTime.now())
                .build();
        bookingEventPublisher.publishBookingCancelled(cancelledEvent);

        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public List<FlightSeatResponse> getFlightSeats(Long flightId) {
        log.info("Fetching flight seats for flightId={}", flightId);
        List<FlightSeat> seats = new ArrayList<>(flightSeatRepository.findByFlightIdOrderBySeatNumberAsc(flightId));

        Set<String> existingSeatNumbers = seats.stream()
                .map(FlightSeat::getSeatNumber)
                .collect(Collectors.toSet());

        List<FlightSeat> toAdd = new ArrayList<>();
        String[] cols = {"A", "B", "C", "D", "E", "F"};
        for (int row = 1; row <= 5; row++) {
            CabinClass cabin = (row == 1) ? CabinClass.BUSINESS : CabinClass.ECONOMY;
            for (String col : cols) {
                String seatNum = row + col;
                if (!existingSeatNumbers.contains(seatNum)) {
                    toAdd.add(FlightSeat.builder()
                            .flightId(flightId)
                            .seatNumber(seatNum)
                            .cabinClass(cabin)
                            .seatStatus(SeatStatus.AVAILABLE)
                            .build());
                }
            }
        }

        if (!toAdd.isEmpty()) {
            List<FlightSeat> saved = flightSeatRepository.saveAll(toAdd);
            seats.addAll(saved);
            seats.sort(Comparator.comparing(FlightSeat::getSeatNumber));
        }

        // Auto-revert expired holds
        LocalDateTime now = LocalDateTime.now();
        for (FlightSeat seat : seats) {
            if (seat.getSeatStatus() == SeatStatus.HELD && seat.getHoldExpiresAt() != null && seat.getHoldExpiresAt().isBefore(now)) {
                seat.setSeatStatus(SeatStatus.AVAILABLE);
                seat.setBookingId(null);
                seat.setUserId(null);
                seat.setHoldExpiresAt(null);
                flightSeatRepository.save(seat);
            }
        }

        return seats.stream()
                .map(bookingMapper::toSeatResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<FlightSeatResponse> adminBlockSeats(AdminBlockSeatsRequest request) {
        log.info("Admin blocking seats for flightId={}, seats={}, reason={}",
                request.getFlightId(), request.getSeatNumbers(), request.getReason());

        List<FlightSeatResponse> responses = new ArrayList<>();
        for (String seatNum : request.getSeatNumbers()) {
            String formatted = seatNum.trim().toUpperCase();
            FlightSeat seat = flightSeatRepository.findByFlightIdAndSeatNumber(request.getFlightId(), formatted)
                    .orElseGet(() -> FlightSeat.builder()
                            .flightId(request.getFlightId())
                            .seatNumber(formatted)
                            .cabinClass(formatted.startsWith("1") ? CabinClass.BUSINESS : CabinClass.ECONOMY)
                            .build());

            if (seat.getSeatStatus() == SeatStatus.RESERVED) {
                throw new InvalidBookingStateException("Cannot block seat " + formatted + " because it is currently RESERVED");
            }

            seat.setSeatStatus(SeatStatus.BLOCKED);
            seat.setBlockedReason(request.getReason() != null ? request.getReason() : "Admin blocked");
            seat.setBookingId(null);
            seat.setUserId(null);
            seat.setHoldExpiresAt(null);

            FlightSeat saved = flightSeatRepository.save(seat);
            responses.add(bookingMapper.toSeatResponse(saved));
        }
        return responses;
    }

    @Override
    @Transactional
    public List<FlightSeatResponse> adminUnblockSeats(AdminUnblockSeatsRequest request) {
        log.info("Admin unblocking seats for flightId={}, seats={}", request.getFlightId(), request.getSeatNumbers());
        List<FlightSeatResponse> responses = new ArrayList<>();
        for (String seatNum : request.getSeatNumbers()) {
            String formatted = seatNum.trim().toUpperCase();
            FlightSeat seat = flightSeatRepository.findByFlightIdAndSeatNumber(request.getFlightId(), formatted)
                    .orElseThrow(() -> new InvalidBookingStateException("Seat " + formatted + " does not exist for flight " + request.getFlightId()));

            if (seat.getSeatStatus() == SeatStatus.BLOCKED) {
                seat.setSeatStatus(SeatStatus.AVAILABLE);
                seat.setBlockedReason(null);
                FlightSeat saved = flightSeatRepository.save(seat);
                responses.add(bookingMapper.toSeatResponse(saved));
            } else {
                responses.add(bookingMapper.toSeatResponse(seat));
            }
        }
        return responses;
    }

    @Override
    @Transactional
    public BookingResponse adminBlockBooking(Long bookingId, AdminBlockBookingRequest request) {
        log.info("Admin blocking bookingId={}, reason={}", bookingId, request.getReason());
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        BookingStatus previous = booking.getBookingStatus();
        booking.setBookingStatus(BookingStatus.BLOCKED);
        booking.setBlockedReason(request.getReason());
        Booking saved = bookingRepository.save(booking);

        saveStatusHistory(saved.getId(), previous, BookingStatus.BLOCKED, "Admin blocked booking: " + request.getReason());
        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse adminUnblockBooking(Long bookingId) {
        log.info("Admin unblocking bookingId={}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getBookingStatus() != BookingStatus.BLOCKED) {
            throw new InvalidBookingStateException("Booking is not BLOCKED. Current status: " + booking.getBookingStatus());
        }

        BookingStatus newStatus = (booking.getPaymentStatus() == PaymentStatus.SUCCESS)
                ? BookingStatus.CONFIRMED
                : BookingStatus.PAYMENT_PENDING;

        booking.setBookingStatus(newStatus);
        booking.setBlockedReason(null);
        Booking saved = bookingRepository.save(booking);

        saveStatusHistory(saved.getId(), BookingStatus.BLOCKED, newStatus, "Admin unblocked booking");
        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {
        log.info("Fetching booking by id={}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByPnr(String pnr) {
        log.info("Fetching booking by PNR={}", pnr);
        Booking booking = bookingRepository.findByPnr(pnr.toUpperCase())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with PNR: " + pnr));
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        log.info("Fetching all bookings for userId={}", userId);
        return bookingRepository.findByUserId(userId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingStatusHistoryResponse> getBookingStatusHistory(Long bookingId) {
        log.info("Fetching status history for bookingId={}", bookingId);
        if (!bookingRepository.existsById(bookingId)) {
            throw new BookingNotFoundException("Booking not found with id: " + bookingId);
        }
        return historyRepository.findByBookingIdOrderByChangedAtAsc(bookingId).stream()
                .map(bookingMapper::toHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingCancellationResponse cancelBooking(Long bookingId, CancelBookingRequest request) {
        log.info("Initiating cancellation for bookingId={}, reason={}", bookingId, request.getReason());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        BookingStatus currentStatus = booking.getBookingStatus();

        if (currentStatus == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Booking is already CANCELLED");
        }

        if (currentStatus == BookingStatus.EXPIRED || currentStatus == BookingStatus.FAILED) {
            throw new InvalidBookingStateException("Cannot cancel booking in status: " + currentStatus);
        }

        // Release seats associated with this booking
        releaseSeatsForBooking(bookingId);

        int passengerCount = (booking.getPassengers() != null && !booking.getPassengers().isEmpty())
                ? booking.getPassengers().size()
                : 1;
        BigDecimal feePerPassenger = BigDecimal.valueOf(1500);
        BigDecimal totalFee = feePerPassenger.multiply(BigDecimal.valueOf(passengerCount));

        BigDecimal originalTotal = booking.getTotalAmount();
        BigDecimal cancellationFee = totalFee.min(originalTotal);
        BigDecimal refundAmount = originalTotal.subtract(cancellationFee).max(BigDecimal.ZERO);

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.REFUNDED);
        booking.setCancellationFee(cancellationFee);
        booking.setRefundAmount(refundAmount);
        booking.setCancellationReason(request.getReason());

        Booking saved = bookingRepository.save(booking);

        saveStatusHistory(saved.getId(), currentStatus, BookingStatus.CANCELLED, request.getReason());

        BookingCancelledEvent cancelledEvent = BookingCancelledEvent.builder()
                .eventId("EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .bookingId(saved.getId())
                .pnr(saved.getPnr())
                .bookingReference(saved.getBookingReference())
                .originalAmount(originalTotal)
                .cancellationFee(cancellationFee)
                .refundAmount(refundAmount)
                .currency(saved.getCurrency())
                .reason(request.getReason())
                .timestamp(LocalDateTime.now())
                .build();
        bookingEventPublisher.publishBookingCancelled(cancelledEvent);

        log.info("Booking {} successfully cancelled. Cancellation fee: {}, Refund amount: {}",
                saved.getBookingReference(), cancellationFee, refundAmount);

        return BookingCancellationResponse.builder()
                .bookingReference(saved.getBookingReference())
                .pnr(saved.getPnr())
                .originalAmount(originalTotal)
                .cancellationFee(cancellationFee)
                .refundAmount(refundAmount)
                .currency(saved.getCurrency())
                .status(BookingStatus.CANCELLED)
                .reason(request.getReason())
                .build();
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        log.info("Confirming booking id={}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getBookingStatus() != BookingStatus.PAYMENT_PENDING &&
            booking.getBookingStatus() != BookingStatus.PENDING &&
            booking.getBookingStatus() != BookingStatus.HELD) {
            throw new InvalidBookingStateException("Cannot confirm booking from current status: " + booking.getBookingStatus());
        }

        // If it was held, verify it hasn't expired
        if (booking.getBookingStatus() == BookingStatus.HELD &&
            booking.getHoldExpiresAt() != null &&
            booking.getHoldExpiresAt().isBefore(LocalDateTime.now())) {

            releaseSeatsForBooking(bookingId);
            booking.setBookingStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            saveStatusHistory(bookingId, BookingStatus.HELD, BookingStatus.EXPIRED, "Hold expired before confirmation");
            throw new InvalidBookingStateException("Booking hold has expired");
        }

        // Transition held seats to RESERVED
        List<FlightSeat> seats = flightSeatRepository.findByBookingId(bookingId);
        for (FlightSeat seat : seats) {
            seat.setSeatStatus(SeatStatus.RESERVED);
            seat.setHoldExpiresAt(null);
            flightSeatRepository.save(seat);
        }

        BookingStatus previous = booking.getBookingStatus();
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.SUCCESS);
        booking.setHoldExpiresAt(null);
        Booking saved = bookingRepository.save(booking);

        saveStatusHistory(saved.getId(), previous, BookingStatus.CONFIRMED, "Payment completed successfully");

        BookingConfirmedEvent event = BookingConfirmedEvent.builder()
                .eventId("EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .bookingId(saved.getId())
                .pnr(saved.getPnr())
                .bookingReference(saved.getBookingReference())
                .totalAmount(saved.getTotalAmount())
                .currency(saved.getCurrency())
                .timestamp(LocalDateTime.now())
                .build();
        bookingEventPublisher.publishBookingConfirmed(event);

        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings(Long userId) {
        log.info("Fetching all bookings, userId={}", userId);
        List<Booking> bookings = (userId != null)
                ? bookingRepository.findByUserId(userId)
                : bookingRepository.findAll();
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void applyMemberPricingAndPassengers(Booking booking, FareValidationResponse fareResponse, List<PassengerRequest> passengerRequests) {
        BigDecimal basePrice = fareResponse.getBasePrice();
        BigDecimal taxAmount = fareResponse.getTaxAmount();

        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        BigDecimal adultTotal = BigDecimal.ZERO;
        BigDecimal childTotal = BigDecimal.ZERO;
        BigDecimal infantTotal = BigDecimal.ZERO;

        BigDecimal totalBasePrice = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (PassengerRequest pReq : passengerRequests) {
            BigDecimal indBase;
            BigDecimal indTax;
            BigDecimal indTot;

            PassengerType type = pReq.getPassengerType() != null ? pReq.getPassengerType() : PassengerType.ADULT;

            switch (type) {
                case CHILD:
                    childCount++;
                    indBase = basePrice.multiply(new BigDecimal("0.75")).setScale(2, RoundingMode.HALF_UP);
                    indTax = taxAmount.multiply(new BigDecimal("0.75")).setScale(2, RoundingMode.HALF_UP);
                    indTot = indBase.add(indTax);
                    childTotal = childTotal.add(indTot);
                    break;
                case INFANT:
                    infantCount++;
                    indBase = basePrice.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
                    indTax = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    indTot = indBase;
                    infantTotal = infantTotal.add(indTot);
                    break;
                case ADULT:
                default:
                    adultCount++;
                    indBase = basePrice.setScale(2, RoundingMode.HALF_UP);
                    indTax = taxAmount.setScale(2, RoundingMode.HALF_UP);
                    indTot = indBase.add(indTax);
                    adultTotal = adultTotal.add(indTot);
                    break;
            }

            totalBasePrice = totalBasePrice.add(indBase);
            totalTaxAmount = totalTaxAmount.add(indTax);
            grandTotal = grandTotal.add(indTot);

            Passenger passenger = bookingMapper.toPassengerEntity(pReq);
            passenger.setIndividualBasePrice(indBase);
            passenger.setIndividualTaxAmount(indTax);
            passenger.setIndividualTotalAmount(indTot);
            booking.addPassenger(passenger);
        }

        booking.setAdultCount(adultCount);
        booking.setChildCount(childCount);
        booking.setInfantCount(infantCount);
        booking.setAdultTotal(adultTotal);
        booking.setChildTotal(childTotal);
        booking.setInfantTotal(infantTotal);
        booking.setBasePrice(totalBasePrice);
        booking.setTaxAmount(totalTaxAmount);
        booking.setTotalAmount(grandTotal);
    }

    private void assignSeats(Booking booking, List<PassengerRequest> passengerRequests, SeatStatus status, LocalDateTime holdExpiresAt) {
        for (PassengerRequest pReq : passengerRequests) {
            if (pReq.getSeatNumber() != null && !pReq.getSeatNumber().isBlank()) {
                String seatNum = pReq.getSeatNumber().trim().toUpperCase();
                FlightSeat seat = flightSeatRepository.findByFlightIdAndSeatNumber(booking.getFlightId(), seatNum)
                        .orElseGet(() -> {
                            CabinClass cabinClass = seatNum.startsWith("1") ? CabinClass.BUSINESS : CabinClass.ECONOMY;
                            return FlightSeat.builder()
                                    .flightId(booking.getFlightId())
                                    .seatNumber(seatNum)
                                    .cabinClass(cabinClass)
                                    .seatStatus(SeatStatus.AVAILABLE)
                                    .build();
                        });

                if (!seat.isCurrentlyAvailable() && !booking.getId().equals(seat.getBookingId())) {
                    throw new InvalidBookingStateException("Seat " + seatNum + " is currently " + seat.getSeatStatus());
                }

                seat.setSeatStatus(status);
                seat.setBookingId(booking.getId());
                seat.setUserId(booking.getUserId());
                seat.setHoldExpiresAt(holdExpiresAt);
                seat.setBlockedReason(null);
                flightSeatRepository.save(seat);
            }
        }
    }

    private void releaseSeatsForBooking(Long bookingId) {
        List<FlightSeat> seats = flightSeatRepository.findByBookingId(bookingId);
        for (FlightSeat seat : seats) {
            seat.setSeatStatus(SeatStatus.AVAILABLE);
            seat.setBookingId(null);
            seat.setUserId(null);
            seat.setHoldExpiresAt(null);
            seat.setBlockedReason(null);
            flightSeatRepository.save(seat);
        }
    }

    private void publishCreatedEvent(Booking savedBooking, int passengerCount) {
        BookingCreatedEvent createdEvent = BookingCreatedEvent.builder()
                .eventId("EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .bookingId(savedBooking.getId())
                .pnr(savedBooking.getPnr())
                .bookingReference(savedBooking.getBookingReference())
                .userId(savedBooking.getUserId())
                .flightId(savedBooking.getFlightId())
                .fareId(savedBooking.getFareId())
                .fareCode(savedBooking.getFareCode())
                .totalAmount(savedBooking.getTotalAmount())
                .currency(savedBooking.getCurrency())
                .passengerCount(passengerCount)
                .timestamp(LocalDateTime.now())
                .build();
        bookingEventPublisher.publishBookingCreated(createdEvent);
    }

    private void saveStatusHistory(Long bookingId, BookingStatus prev, BookingStatus next, String reason) {
        BookingStatusHistory history = BookingStatusHistory.builder()
                .bookingId(bookingId)
                .previousStatus(prev)
                .newStatus(next)
                .reason(reason)
                .changedAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);
    }

    private void validatePassengers(List<PassengerRequest> passengers) {
        for (PassengerRequest p : passengers) {
            int age = Period.between(p.getDateOfBirth(), LocalDate.now()).getYears();
            if (p.getPassengerType() == PassengerType.INFANT && age > 2) {
                throw new InvalidBookingStateException("Passenger " + p.getFirstName() + " marked as INFANT but age is " + age + " (must be <= 2 years)");
            }
            if (p.getPassengerType() == PassengerType.CHILD && (age < 2 || age > 12)) {
                throw new InvalidBookingStateException("Passenger " + p.getFirstName() + " marked as CHILD but age is " + age + " (must be between 2 and 12 years)");
            }
        }
    }
}
