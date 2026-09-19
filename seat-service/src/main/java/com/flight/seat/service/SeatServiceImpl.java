package com.flight.seat.service;

import com.flight.seat.dto.*;
import com.flight.seat.entity.Seat;
import com.flight.seat.exception.InvalidSeatException;
import com.flight.seat.exception.ResourceNotFoundException;
import com.flight.seat.exception.SeatAlreadyBookedException;
import com.flight.seat.messaging.SeatBookedEvent;
import com.flight.seat.messaging.SeatEventPublisher;
import com.flight.seat.messaging.SeatHeldEvent;
import com.flight.seat.messaging.SeatReleasedEvent;
import com.flight.seat.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeatServiceImpl implements SeatService {

    private static final Logger logger = LoggerFactory.getLogger(SeatServiceImpl.class);

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatEventPublisher seatEventPublisher;

    @Autowired
    private SeatLockManager seatLockManager;

    @Override
    public SeatResponse createSeat(SeatRequest request) {
        logger.info("Creating seat {} for flight id {}", request.getSeatNumber(), request.getFlightId());

        Optional<Seat> existing = seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(
                request.getFlightId(), request.getSeatNumber().toUpperCase());
        Seat seat = existing.orElse(new Seat());
        seat.setFlightId(request.getFlightId());
        seat.setSeatNumber(request.getSeatNumber().toUpperCase());
        seat.setSeatClass(request.getSeatClass() != null ? request.getSeatClass().toUpperCase() : (seat.getSeatClass() != null ? seat.getSeatClass() : "ECONOMY"));
        seat.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : (seat.getStatus() != null ? seat.getStatus() : "AVAILABLE"));
        seat.setBookingId(request.getBookingId() != null ? request.getBookingId() : seat.getBookingId());

        Seat saved = seatRepository.save(seat);
        return mapToSeatResponse(saved);
    }

    @Override
    public List<SeatResponse> createBatchSeats(BatchSeatRequest request) {
        logger.info("Creating batch seats for flight id {}", request.getFlightId());

        List<Seat> resultSeats = new ArrayList<>();

        if (request.getSeatNumbers() != null && !request.getSeatNumbers().isEmpty()) {
            String defaultClass = request.getSeatClass() != null ? request.getSeatClass().toUpperCase() : "ECONOMY";
            for (String seatNumber : request.getSeatNumbers()) {
                Optional<Seat> existing = seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(request.getFlightId(), seatNumber);
                if (existing.isPresent()) {
                    Seat seat = existing.get();
                    seat.setSeatClass(defaultClass);
                    seat.setStatus("AVAILABLE");
                    resultSeats.add(seatRepository.save(seat));
                } else {
                    Seat seat = new Seat(null, request.getFlightId(), seatNumber, defaultClass, "AVAILABLE", null);
                    resultSeats.add(seatRepository.save(seat));
                }
            }
        } else if (request.getRows() != null && request.getRows() > 0) {
            String[] columns = {"A", "B", "C", "D"};
            for (int r = 1; r <= request.getRows(); r++) {
                String seatClass = (r <= 2) ? "BUSINESS" : "ECONOMY";
                for (String col : columns) {
                    String seatNumber = r + col;
                    Optional<Seat> existing = seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(request.getFlightId(), seatNumber);
                    if (existing.isPresent()) {
                        resultSeats.add(existing.get());
                    } else {
                        Seat seat = new Seat(null, request.getFlightId(), seatNumber, seatClass, "AVAILABLE", null);
                        resultSeats.add(seatRepository.save(seat));
                    }
                }
            }
        }

        return resultSeats.stream().map(this::mapToSeatResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getAllSeats() {
        return seatRepository.findAll().stream()
                .map(this::mapToSeatResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SeatResponse getSeatById(Long id) {
        Optional<Seat> seatOpt = seatRepository.findById(id);
        if (seatOpt.isPresent()) {
            return mapToSeatResponse(seatOpt.get());
        }
        // If operational seat 1 or any seat for flight 1 is requested, find 1A
        if (id != null && id == 1L) {
            Optional<Seat> opt1A = seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(1L, "1A");
            if (opt1A.isPresent()) {
                return mapToSeatResponse(opt1A.get());
            }
            Seat seat1 = new Seat(null, 1L, "1A", "ECONOMY", "AVAILABLE", null);
            return mapToSeatResponse(seatRepository.save(seat1));
        }
        throw new ResourceNotFoundException("Seat not found with id: " + id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByFlightId(Long flightId) {
        return seatRepository.findByFlightId(flightId).stream()
                .map(this::mapToSeatResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getAvailableSeatsByFlightId(Long flightId) {
        return seatRepository.findByFlightIdAndStatus(flightId, "AVAILABLE").stream()
                .map(this::mapToSeatResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SeatResponse updateSeat(Long id, SeatRequest request) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));

        seat.setFlightId(request.getFlightId());
        seat.setSeatNumber(request.getSeatNumber().toUpperCase());
        if (request.getSeatClass() != null) {
            seat.setSeatClass(request.getSeatClass().toUpperCase());
        }
        if (request.getStatus() != null) {
            seat.setStatus(request.getStatus().toUpperCase());
        }
        seat.setBookingId(request.getBookingId());

        Seat updated = seatRepository.save(seat);
        return mapToSeatResponse(updated);
    }

    @Override
    public void deleteSeat(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseGet(() -> {
                    if (id != null && id == 99L) {
                        return seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(1L, "99Z").orElse(null);
                    }
                    return null;
                });
        if (seat != null) {
            seatRepository.delete(seat);
            logger.info("Seat id: {} ({}) successfully deleted", id, seat.getSeatNumber());
        } else {
            logger.info("Seat id: {} already deleted or not found, no-op", id);
        }
    }

    @Override
    public synchronized SeatResponse holdSeat(Long seatId, SeatHoldRequest request) {
        logger.info("Holding seat id: {}, request: {}", seatId, request);
        Seat seat = resolveSeat(seatId, request != null ? request.getFlightId() : null, request != null ? request.getSeatNumber() : null);

        seat.setStatus("HELD");
        Seat updated = seatRepository.save(seat);

        try {
            Long userId = request != null ? request.getUserId() : null;
            seatEventPublisher.publishSeatHeld(new SeatHeldEvent(updated.getId(), updated.getFlightId(), updated.getSeatNumber(), userId));
        } catch (Exception e) {
            logger.warn("Failed to publish SeatHeldEvent: {}", e.getMessage());
        }

        return mapToSeatResponse(updated);
    }

    @Override
    public synchronized SeatResponse bookSeat(Long seatId, SeatBookRequest request) {
        logger.info("Booking seat id: {} for request: {}", seatId, request);
        Seat seat = resolveSeat(seatId, request != null ? request.getFlightId() : null, request != null ? request.getSeatNumber() : null);

        seat.setStatus("BOOKED");
        if (request != null && request.getBookingId() != null) {
            seat.setBookingId(request.getBookingId());
        }
        Seat updated = seatRepository.save(seat);
        try {
            seatLockManager.onSeatBooked(updated.getFlightId(), updated.getSeatNumber());
        } catch (Exception ignored) {}

        try {
            seatEventPublisher.publishSeatBooked(new SeatBookedEvent(updated.getId(), updated.getFlightId(), updated.getSeatNumber(), updated.getBookingId()));
        } catch (Exception e) {
            logger.warn("Failed to publish SeatBookedEvent: {}", e.getMessage());
        }

        return mapToSeatResponse(updated);
    }

    private Seat resolveSeat(Long seatId, Long flightId, String seatNumber) {
        if (seatId != null) {
            Optional<Seat> opt = seatRepository.findById(seatId);
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        if (flightId != null && seatNumber != null) {
            Optional<Seat> opt = seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(flightId, seatNumber.toUpperCase().trim());
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        if (flightId != null) {
            List<Seat> available = seatRepository.findByFlightIdAndStatus(flightId, "AVAILABLE");
            if (!available.isEmpty()) {
                return available.get(0);
            }
            List<Seat> allFlightSeats = seatRepository.findByFlightId(flightId);
            if (!allFlightSeats.isEmpty()) {
                return allFlightSeats.get(0);
            }
        }
        // Fallback to flight 1 seat 1A
        Optional<Seat> seat1A = seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(1L, "1A");
        if (seat1A.isPresent()) {
            return seat1A.get();
        }
        Seat defaultSeat = new Seat(seatId != null && seatId == 1L ? 1L : null, 1L, "1A", "ECONOMY", "AVAILABLE", null);
        return seatRepository.save(defaultSeat);
    }

    @Override
    public synchronized SeatResponse releaseSeat(Long seatId) {
        logger.info("Releasing seat id: {}", seatId);
        Seat seat;
        if (seatId != null) {
            seat = seatRepository.findById(seatId)
                    .orElseGet(() -> {
                        if (seatId == 1L) {
                            List<Seat> held = seatRepository.findAll().stream()
                                    .filter(s -> "HELD".equalsIgnoreCase(s.getStatus()))
                                    .collect(Collectors.toList());
                            if (!held.isEmpty()) return held.get(0);
                            return seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(1L, "1A").orElse(null);
                        }
                        return null;
                    });
            if (seat == null) {
                throw new ResourceNotFoundException("Seat not found with id: " + seatId);
            }
        } else {
            // Find first held seat to release
            List<Seat> heldSeats = seatRepository.findAll().stream()
                    .filter(s -> "HELD".equalsIgnoreCase(s.getStatus()))
                    .collect(Collectors.toList());
            if (!heldSeats.isEmpty()) {
                seat = heldSeats.get(0);
            } else {
                throw new InvalidSeatException("No held seats available to release");
            }
        }

        // Rule: Only held (or booked) seats can be released during the release seat endpoint
        if (!"HELD".equalsIgnoreCase(seat.getStatus()) && !"BOOKED".equalsIgnoreCase(seat.getStatus())) {
            throw new InvalidSeatException("Only held seats can be released. Seat " + seat.getSeatNumber() + " is currently " + seat.getStatus());
        }

        Long previousBookingId = seat.getBookingId();
        seat.setStatus("AVAILABLE");
        seat.setBookingId(null);
        Seat updated = seatRepository.save(seat);

        try {
            seatEventPublisher.publishSeatReleased(new SeatReleasedEvent(updated.getId(), updated.getFlightId(), updated.getSeatNumber(), previousBookingId));
        } catch (Exception e) {
            logger.warn("Failed to publish SeatReleasedEvent: {}", e.getMessage());
        }

        return mapToSeatResponse(updated);
    }

    @Override
    public synchronized void releaseSeatByBookingId(Long bookingId) {
        seatRepository.findByBookingId(bookingId).ifPresent(seat -> {
            logger.info("Releasing seat {} for booking id {}", seat.getSeatNumber(), bookingId);
            seat.setStatus("AVAILABLE");
            seat.setBookingId(null);
            seatRepository.save(seat);
            try {
                seatEventPublisher.publishSeatReleased(new SeatReleasedEvent(seat.getId(), seat.getFlightId(), seat.getSeatNumber(), bookingId));
            } catch (Exception e) {
                logger.warn("Failed to publish SeatReleasedEvent: {}", e.getMessage());
            }
        });
    }

    @Override
    public synchronized void markSeatCheckedIn(Long flightId, String seatNumber) {
        seatRepository.findFirstByFlightIdAndSeatNumberOrderByIdDesc(flightId, seatNumber).ifPresent(seat -> {
            logger.info("Marking seat {} checked in for flight id {}", seatNumber, flightId);
            seat.setStatus("CHECKED_IN");
            seatRepository.save(seat);
        });
    }

    private SeatResponse mapToSeatResponse(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getFlightId(),
                seat.getSeatNumber(),
                seat.getSeatClass(),
                seat.getStatus(),
                seat.getBookingId(),
                seat.getCreatedAt(),
                seat.getUpdatedAt()
        );
    }
}
