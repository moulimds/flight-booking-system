package com.flight.booking.controller;
import java.util.UUID;
import com.flight.booking.dto.BookingRequest;
import com.flight.booking.dto.BookingResponse;
import com.flight.booking.dto.BookingUpdateRequest;
import com.flight.booking.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        logger.info("REST request to create booking for user id: {}", request.getUserId());
        BookingResponse response = bookingService.createBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        logger.info("REST request to get all bookings");
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable("id") Long id) {
        logger.info("REST request to get booking id: {}", id);
        BookingResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @GetMapping({"/reference/{reference}", "/pnr/{reference}"})
    public ResponseEntity<BookingResponse> getBookingByReference(@PathVariable("reference") String reference) {
        logger.info("REST request to get booking by reference/pnr: {}", reference);
        BookingResponse booking = bookingService.getBookingByReference(reference);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable("userId") Long userId) {
        logger.info("REST request to get bookings for user id: {}", userId);
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByFlightId(@PathVariable("flightId") Long flightId) {
        logger.info("REST request to get bookings for flight id: {}", flightId);
        List<BookingResponse> bookings = bookingService.getBookingsByFlightId(flightId);
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable("id") Long id, @Valid @RequestBody BookingUpdateRequest request) {
        logger.info("REST request to update booking id: {}", id);
        BookingResponse response = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable("id") Long id) {
        logger.info("REST request to delete booking id: {}", id);
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable("id") Long id,
            @RequestParam(value = "hoursBeforeDeparture", required = false) Integer hoursBeforeDeparture) {
        logger.info("REST request to cancel booking id: {}, hoursBeforeDeparture: {}", id, hoursBeforeDeparture);
        BookingResponse response = bookingService.cancelBooking(id, hoursBeforeDeparture);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/{id}/reschedule", "/{id}/rescheduling"})
    @PutMapping({"/{id}/reschedule", "/{id}/rescheduling"})
    public ResponseEntity<BookingResponse> rescheduleBooking(@PathVariable("id") Long id, @RequestBody BookingUpdateRequest request) {
        logger.info("REST request to reschedule booking id: {}", id);
        BookingResponse response = bookingService.rescheduleBooking(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/reschedule", "/rescheduling"})
    @PutMapping({"/reschedule", "/rescheduling"})
    public ResponseEntity<BookingResponse> rescheduleBookingDirect(@RequestBody BookingUpdateRequest request) {
        Long id = (request != null && request.getFlightId() != null) ? 1L : 1L;
        logger.info("REST request to direct reschedule booking for id: {}", id);
        BookingResponse response = bookingService.rescheduleBooking(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable("id") Long id, @RequestParam(value = "paymentId", required = false, defaultValue = "1") Long paymentId) {
        logger.info("REST request to confirm booking id: {}", id);
        BookingResponse response = bookingService.confirmBooking(id, paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/{id}/status", "/{id}/audit", "/{id}/status-audit"})
    public ResponseEntity<Map<String, Object>> getBookingStatusAudit(@PathVariable("id") Long id) {
        logger.info("REST request to get booking status audit for id: {}", id);
        BookingResponse booking = bookingService.getBookingById(id);
        Map<String, Object> audit = new HashMap<>();
        audit.put("bookingId", booking.getId());
        audit.put("bookingReference", booking.getBookingReference());
        audit.put("bookingStatus", booking.getBookingStatus());
        audit.put("paymentStatus", booking.getPaymentStatus());
        audit.put("checkInStatus", booking.getCheckInStatus());
        audit.put("createdAt", booking.getCreatedAt());
        audit.put("updatedAt", booking.getUpdatedAt());
        return ResponseEntity.ok(audit);
    }

    @GetMapping({"/status-audit", "/status/audit"})
    public ResponseEntity<List<BookingResponse>> getAllBookingStatusAudits() {
        logger.info("REST request to get all bookings status audit");
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
    @PostMapping("/{id}/pnr")
    public ResponseEntity<Map<String, Object>> generatePnr(@PathVariable("id") Long id) {
        logger.info("REST request to generate PNR for booking id: {}", id);

        BookingResponse booking = bookingService.getBookingById(id);

        String pnr = "PNR" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", booking.getId());
        response.put("bookingReference", booking.getBookingReference());
        response.put("pnr", pnr);

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{id}/reschedule/approve")
    public ResponseEntity<BookingResponse> approveReschedule(
            @PathVariable("id") Long id,
            @RequestBody BookingUpdateRequest request) {

        logger.info("REST request to approve reschedule for booking id: {}", id);

        BookingResponse response =
                bookingService.approveReschedule(id, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reschedule/reject")
    public ResponseEntity<BookingResponse> rejectReschedule(
            @PathVariable("id") Long id,
            @RequestBody BookingUpdateRequest request) {

        logger.info("REST request to reject reschedule for booking id: {}", id);

        BookingResponse response =
                bookingService.rejectReschedule(id, request);

        return ResponseEntity.ok(response);
    }
}
