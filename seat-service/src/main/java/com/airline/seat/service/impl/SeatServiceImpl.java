package com.airline.seat.service.impl;

import com.airline.seat.dto.*;
import com.airline.seat.entity.Seat;
import com.airline.seat.entity.SeatClass;
import com.airline.seat.entity.SeatInventory;
import com.airline.seat.entity.SeatStatus;
import com.airline.seat.exception.InvalidSeatOperationException;
import com.airline.seat.exception.ResourceNotFoundException;
import com.airline.seat.exception.SeatHoldExpiredException;
import com.airline.seat.exception.SeatUnavailableException;
import com.airline.seat.repository.SeatInventoryRepository;
import com.airline.seat.repository.SeatRepository;
import com.airline.seat.service.SeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeatServiceImpl implements SeatService {

    private static final Logger log = LoggerFactory.getLogger(SeatServiceImpl.class);

    private final SeatRepository seatRepository;
    private final SeatInventoryRepository inventoryRepository;

    public SeatServiceImpl(SeatRepository seatRepository, SeatInventoryRepository inventoryRepository) {
        this.seatRepository = seatRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public List<SeatResponse> holdSeats(SeatHoldRequest request) {
        log.info("Attempting to hold seats {} on schedule {} for user {}",
                request.getSeatIds(), request.getFlightScheduleId(), request.getUserId());

        int duration = (request.getHoldDurationMinutes() != null && request.getHoldDurationMinutes() > 0)
                ? request.getHoldDurationMinutes() : 10;
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(duration);

        List<SeatResponse> responses = new ArrayList<>();

        for (Long seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));

            SeatInventory inventory = inventoryRepository
                    .findByFlightScheduleIdAndSeatId(request.getFlightScheduleId(), seatId)
                    .orElseGet(() -> new SeatInventory(seat, request.getFlightScheduleId(), SeatStatus.AVAILABLE));

            // Check if seat can be held
            boolean isAvailable = (inventory.getStatus() == SeatStatus.AVAILABLE);
            boolean isExpiredHold = inventory.isHoldExpired();
            boolean isHeldBySameUser = (inventory.getStatus() == SeatStatus.HELD &&
                    request.getUserId().equals(inventory.getHeldByUserId()));

            if (!isAvailable && !isExpiredHold && !isHeldBySameUser) {
                throw new SeatUnavailableException(String.format(
                        "Seat %s (ID: %d) is currently %s and cannot be held.",
                        seat.getSeatNumber(), seatId, inventory.getStatus()));
            }

            inventory.setStatus(SeatStatus.HELD);
            inventory.setHeldByUserId(request.getUserId());
            inventory.setHoldExpiresAt(expirationTime);
            inventory.setBookingReference(null);
            inventory.setBlockedReason(null);
            inventory.setBlockedBy(null);

            SeatInventory saved = inventoryRepository.save(inventory);
            responses.add(SeatResponse.from(seat, saved));
        }

        return responses;
    }

    @Override
    public List<SeatResponse> reserveSeats(SeatReserveRequest request) {
        log.info("Attempting to reserve seats {} on schedule {} with booking reference {}",
                request.getSeatIds(), request.getFlightScheduleId(), request.getBookingReference());

        List<SeatResponse> responses = new ArrayList<>();

        for (Long seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));

            SeatInventory inventory = inventoryRepository
                    .findByFlightScheduleIdAndSeatId(request.getFlightScheduleId(), seatId)
                    .orElseGet(() -> new SeatInventory(seat, request.getFlightScheduleId(), SeatStatus.AVAILABLE));

            if (inventory.getStatus() == SeatStatus.HELD) {
                if (inventory.isHoldExpired()) {
                    inventory.setStatus(SeatStatus.AVAILABLE);
                    inventory.setHeldByUserId(null);
                    inventory.setHoldExpiresAt(null);
                    inventoryRepository.save(inventory);
                    throw new SeatHoldExpiredException(String.format(
                            "Hold for seat %s (ID: %d) expired at %s.",
                            seat.getSeatNumber(), seatId, inventory.getHoldExpiresAt()));
                }
                if (!request.getUserId().equals(inventory.getHeldByUserId())) {
                    throw new SeatUnavailableException(String.format(
                            "Seat %s (ID: %d) is currently held by another customer.",
                            seat.getSeatNumber(), seatId));
                }
            } else if (inventory.getStatus() == SeatStatus.RESERVED) {
                throw new SeatUnavailableException(String.format(
                        "Seat %s (ID: %d) is already reserved.",
                        seat.getSeatNumber(), seatId));
            } else if (inventory.getStatus() == SeatStatus.BLOCKED) {
                throw new SeatUnavailableException(String.format(
                        "Seat %s (ID: %d) is blocked for operational maintenance.",
                        seat.getSeatNumber(), seatId));
            }

            inventory.setStatus(SeatStatus.RESERVED);
            inventory.setBookingReference(request.getBookingReference());
            inventory.setHeldByUserId(null);
            inventory.setHoldExpiresAt(null);

            SeatInventory saved = inventoryRepository.save(inventory);
            responses.add(SeatResponse.from(seat, saved));
        }

        return responses;
    }

    @Override
    public List<SeatResponse> releaseSeats(SeatReleaseRequest request) {
        log.info("Releasing seats {} on schedule {} for user {}",
                request.getSeatIds(), request.getFlightScheduleId(), request.getUserId());

        List<SeatResponse> responses = new ArrayList<>();

        for (Long seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));

            SeatInventory inventory = inventoryRepository
                    .findByFlightScheduleIdAndSeatId(request.getFlightScheduleId(), seatId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for seat " + seat.getSeatNumber() + " on schedule " + request.getFlightScheduleId()));

            if (inventory.getStatus() == SeatStatus.HELD) {
                if (!request.getUserId().equals(inventory.getHeldByUserId())) {
                    throw new InvalidSeatOperationException("Cannot release a seat held by another user.");
                }
                inventory.setStatus(SeatStatus.AVAILABLE);
                inventory.setHeldByUserId(null);
                inventory.setHoldExpiresAt(null);
                inventory.setBookingReference(null);

                SeatInventory saved = inventoryRepository.save(inventory);
                responses.add(SeatResponse.from(seat, saved));
            } else {
                responses.add(SeatResponse.from(seat, inventory));
            }
        }

        return responses;
    }

    @Override
    public List<SeatResponse> blockSeats(SeatBlockRequest request) {
        log.info("Blocking seats {} on schedule {} for reason: {}",
                request.getSeatIds(), request.getFlightScheduleId(), request.getReason());

        List<SeatResponse> responses = new ArrayList<>();

        for (Long seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));

            SeatInventory inventory = inventoryRepository
                    .findByFlightScheduleIdAndSeatId(request.getFlightScheduleId(), seatId)
                    .orElseGet(() -> new SeatInventory(seat, request.getFlightScheduleId(), SeatStatus.AVAILABLE));

            if (inventory.getStatus() == SeatStatus.RESERVED) {
                throw new InvalidSeatOperationException(String.format(
                        "Cannot block seat %s because it is already reserved with booking reference %s.",
                        seat.getSeatNumber(), inventory.getBookingReference()));
            }

            inventory.setStatus(SeatStatus.BLOCKED);
            inventory.setBlockedReason(request.getReason());
            inventory.setBlockedBy(request.getBlockedBy());
            inventory.setHeldByUserId(null);
            inventory.setHoldExpiresAt(null);
            inventory.setBookingReference(null);

            SeatInventory saved = inventoryRepository.save(inventory);
            responses.add(SeatResponse.from(seat, saved));
        }

        return responses;
    }

    @Override
    public List<SeatResponse> unblockSeats(SeatUnblockRequest request) {
        log.info("Unblocking seats {} on schedule {}", request.getSeatIds(), request.getFlightScheduleId());

        List<SeatResponse> responses = new ArrayList<>();

        for (Long seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + seatId));

            SeatInventory inventory = inventoryRepository
                    .findByFlightScheduleIdAndSeatId(request.getFlightScheduleId(), seatId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for seat " + seat.getSeatNumber() + " on schedule " + request.getFlightScheduleId()));

            if (inventory.getStatus() == SeatStatus.BLOCKED) {
                inventory.setStatus(SeatStatus.AVAILABLE);
                inventory.setBlockedReason(null);
                inventory.setBlockedBy(null);

                SeatInventory saved = inventoryRepository.save(inventory);
                responses.add(SeatResponse.from(seat, saved));
            } else {
                responses.add(SeatResponse.from(seat, inventory));
            }
        }

        return responses;
    }

    @Override
    public SeatResponse changeSeat(SeatChangeRequest request) {
        log.info("Processing seat change on schedule {}: oldSeatId={}, newSeatId={}, user={}",
                request.getFlightScheduleId(), request.getCurrentSeatId(), request.getNewSeatId(), request.getUserId());

        if (request.getCurrentSeatId().equals(request.getNewSeatId())) {
            throw new InvalidSeatOperationException("Current seat and new seat cannot be identical.");
        }

        Seat currentSeat = seatRepository.findById(request.getCurrentSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Current seat not found with ID: " + request.getCurrentSeatId()));

        Seat newSeat = seatRepository.findById(request.getNewSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("New seat not found with ID: " + request.getNewSeatId()));

        SeatInventory currentInventory = inventoryRepository
                .findByFlightScheduleIdAndSeatId(request.getFlightScheduleId(), request.getCurrentSeatId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No inventory record found for current seat " + currentSeat.getSeatNumber()));

        // Verify that current seat is indeed occupied/held by the requesting user
        if (currentInventory.getStatus() == SeatStatus.HELD) {
            if (!request.getUserId().equals(currentInventory.getHeldByUserId())) {
                throw new InvalidSeatOperationException("Current seat is not held by the requesting user.");
            }
        } else if (currentInventory.getStatus() != SeatStatus.RESERVED) {
            throw new InvalidSeatOperationException("Current seat is not in a HELD or RESERVED state.");
        }

        SeatInventory newInventory = inventoryRepository
                .findByFlightScheduleIdAndSeatId(request.getFlightScheduleId(), request.getNewSeatId())
                .orElseGet(() -> new SeatInventory(newSeat, request.getFlightScheduleId(), SeatStatus.AVAILABLE));

        // Check if new seat is available
        boolean isAvailable = (newInventory.getStatus() == SeatStatus.AVAILABLE);
        boolean isExpired = newInventory.isHoldExpired();

        if (!isAvailable && !isExpired) {
            throw new SeatUnavailableException(String.format(
                    "Desired target seat %s is currently %s and cannot be selected.",
                    newSeat.getSeatNumber(), newInventory.getStatus()));
        }

        // Transfer status & attributes to new seat
        SeatStatus previousStatus = currentInventory.getStatus();
        String bookingRef = currentInventory.getBookingReference();
        LocalDateTime holdExpiry = currentInventory.getHoldExpiresAt();

        // Release old seat
        currentInventory.setStatus(SeatStatus.AVAILABLE);
        currentInventory.setHeldByUserId(null);
        currentInventory.setHoldExpiresAt(null);
        currentInventory.setBookingReference(null);
        inventoryRepository.save(currentInventory);

        // Assign new seat
        newInventory.setStatus(previousStatus);
        newInventory.setBookingReference(bookingRef);
        newInventory.setHeldByUserId(request.getUserId());
        newInventory.setHoldExpiresAt(holdExpiry);
        SeatInventory savedNew = inventoryRepository.save(newInventory);

        return SeatResponse.from(newSeat, savedNew);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailableSeatsCountResponse getAvailableSeatsCount(String flightScheduleId) {
        List<SeatInventory> inventories = inventoryRepository.findByFlightScheduleId(flightScheduleId);
        List<Seat> allSeats = seatRepository.findAll();

        Map<Long, SeatInventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(si -> si.getSeat().getId(), si -> si, (a, b) -> a));

        long total = allSeats.size();
        long available = 0;
        long held = 0;
        long reserved = 0;
        long blocked = 0;

        Map<SeatClass, Long> classBreakdown = new HashMap<>();
        for (SeatClass sc : SeatClass.values()) {
            classBreakdown.put(sc, 0L);
        }

        for (Seat seat : allSeats) {
            SeatInventory inv = inventoryMap.get(seat.getId());
            if (inv == null || inv.getStatus() == SeatStatus.AVAILABLE || inv.isHoldExpired()) {
                available++;
                classBreakdown.put(seat.getSeatClass(), classBreakdown.get(seat.getSeatClass()) + 1);
            } else if (inv.getStatus() == SeatStatus.HELD) {
                held++;
            } else if (inv.getStatus() == SeatStatus.RESERVED) {
                reserved++;
            } else if (inv.getStatus() == SeatStatus.BLOCKED) {
                blocked++;
            }
        }

        return new AvailableSeatsCountResponse(flightScheduleId, total, available, held, reserved, blocked, classBreakdown);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightSeatMapResponse getFlightSeatMap(String flightId, String flightScheduleId) {
        List<Seat> seats = seatRepository.findByFlightId(flightId);
        if (seats.isEmpty()) {
            throw new ResourceNotFoundException("No seats configured for flight ID: " + flightId);
        }

        String scheduleId = (flightScheduleId != null && !flightScheduleId.isBlank()) ? flightScheduleId : "DEFAULT";
        List<SeatInventory> inventories = inventoryRepository.findByFlightScheduleId(scheduleId);

        Map<Long, SeatInventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(si -> si.getSeat().getId(), si -> si, (a, b) -> a));

        List<SeatResponse> seatResponses = seats.stream()
                .map(seat -> {
                    SeatInventory inv = inventoryMap.get(seat.getId());
                    if (inv != null && inv.isHoldExpired()) {
                        // Dynamically reflect expired holds as available
                        inv.setStatus(SeatStatus.AVAILABLE);
                    }
                    return SeatResponse.from(seat, inv);
                })
                .sorted(Comparator.comparing(SeatResponse::getSeatNumber))
                .collect(Collectors.toList());

        return new FlightSeatMapResponse(flightId, scheduleId, seats.size(), seatResponses);
    }

    @Override
    @Scheduled(fixedRate = 30000)
    public void releaseExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<SeatInventory> expiredHolds = inventoryRepository.findByStatusAndHoldExpiresAtBefore(SeatStatus.HELD, now);

        if (!expiredHolds.isEmpty()) {
            log.info("Scheduled task: Releasing {} expired seat holds back to AVAILABLE", expiredHolds.size());
            for (SeatInventory inv : expiredHolds) {
                inv.setStatus(SeatStatus.AVAILABLE);
                inv.setHeldByUserId(null);
                inv.setHoldExpiresAt(null);
                inv.setBookingReference(null);
            }
            inventoryRepository.saveAll(expiredHolds);
        }
    }
}
