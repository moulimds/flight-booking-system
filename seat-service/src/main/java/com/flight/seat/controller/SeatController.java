package com.flight.seat.controller;

import com.flight.seat.dto.*;
import com.flight.seat.service.SeatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private static final Logger logger = LoggerFactory.getLogger(SeatController.class);

    @Autowired
    private SeatService seatService;

    @Autowired
    private com.flight.seat.service.SeatLockManager seatLockManager;

    @PostMapping
    public ResponseEntity<SeatResponse> createSeat(@Valid @RequestBody SeatRequest request) {
        logger.info("REST request to create seat: {} for flight id: {}", request.getSeatNumber(), request.getFlightId());
        SeatResponse response = seatService.createSeat(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<SeatResponse>> createBatchSeats(@Valid @RequestBody BatchSeatRequest request) {
        logger.info("REST request to batch create seats for flight id: {}", request.getFlightId());
        List<SeatResponse> response = seatService.createBatchSeats(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SeatResponse>> getAllSeats() {
        logger.info("REST request to get all seats");
        List<SeatResponse> seats = seatService.getAllSeats();
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> getSeatById(@PathVariable("id") Long id) {
        logger.info("REST request to get seat id: {}", id);
        SeatResponse seat = seatService.getSeatById(id);
        return ResponseEntity.ok(seat);
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<SeatResponse>> getSeatsByFlightId(@PathVariable("flightId") Long flightId) {
        logger.info("REST request to get seats for flight id: {}", flightId);
        List<SeatResponse> seats = seatService.getSeatsByFlightId(flightId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/flight/{flightId}/available")
    public ResponseEntity<List<SeatResponse>> getAvailableSeatsByFlightId(@PathVariable("flightId") Long flightId) {
        logger.info("REST request to get available seats for flight id: {}", flightId);
        List<SeatResponse> seats = seatService.getAvailableSeatsByFlightId(flightId);
        return ResponseEntity.ok(seats);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeatResponse> updateSeat(@PathVariable("id") Long id, @Valid @RequestBody SeatRequest request) {
        logger.info("REST request to update seat id: {}", id);
        SeatResponse response = seatService.updateSeat(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeat(@PathVariable("id") Long id) {
        logger.info("REST request to delete seat id: {}", id);
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping({"/hold", "/{seatId}/hold"})
    public ResponseEntity<SeatResponse> holdSeat(@PathVariable(value = "seatId", required = false) Long seatId, @RequestBody(required = false) SeatHoldRequest request) {
        logger.info("REST request to hold seat id: {}, request: {}", seatId, request);
        SeatResponse response = seatService.holdSeat(seatId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/book", "/{seatId}/book"})
    public ResponseEntity<SeatResponse> bookSeat(@PathVariable(value = "seatId", required = false) Long seatId, @Valid @RequestBody SeatBookRequest request) {
        logger.info("REST request to book seat id: {} for booking id: {}", seatId, request.getBookingId());
        SeatResponse response = seatService.bookSeat(seatId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/release", "/{seatId}/release"})
    public ResponseEntity<SeatResponse> releaseSeat(@PathVariable(value = "seatId", required = false) Long seatId) {
        logger.info("REST request to release seat id: {}", seatId);
        SeatResponse response = seatService.releaseSeat(seatId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/flight/{flightId}/seat/{seatNumber}/checkin")
    public ResponseEntity<Void> markSeatCheckedIn(@PathVariable("flightId") Long flightId, @PathVariable("seatNumber") String seatNumber) {
        logger.info("REST request to mark seat {} as checked in for flight id: {}", seatNumber, flightId);
        seatService.markSeatCheckedIn(flightId, seatNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/lock")
    public ResponseEntity<SeatLockResponse> lockSeat(@Valid @RequestBody SeatLockRequest request) {
        logger.info("REST request to lock seat: {} on flight: {} for user: {} with TTL: {}s",
                request.getSeatNumber(), request.getFlightId(), request.getUserId(), request.getTtlSeconds());
        SeatLockResponse response = seatLockManager.acquireLock(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/unlock")
    public ResponseEntity<Boolean> unlockSeat(@RequestBody SeatLockRequest request) {
        logger.info("REST request to unlock seat: {} on flight: {} for user: {}",
                request.getSeatNumber(), request.getFlightId(), request.getUserId());
        boolean released = seatLockManager.releaseLock(request.getFlightId(), request.getSeatNumber(), request.getUserId());
        return ResponseEntity.ok(released);
    }

    @GetMapping("/lock-status")
    public ResponseEntity<SeatLockResponse> getLockStatus(@RequestParam("flightId") Long flightId, @RequestParam("seatNumber") String seatNumber) {
        SeatLockResponse response = seatLockManager.getLockStatus(flightId, seatNumber);
        return ResponseEntity.ok(response);
    }
}
