package com.example.booking.controller;

import com.example.booking.dto.request.AdminBlockBookingRequest;
import com.example.booking.dto.request.AdminBlockSeatsRequest;
import com.example.booking.dto.request.AdminUnblockSeatsRequest;
import com.example.booking.dto.request.CancelBookingRequest;
import com.example.booking.dto.request.CreateBookingRequest;
import com.example.booking.dto.request.HoldBookingRequest;
import com.example.booking.dto.response.BookingCancellationResponse;
import com.example.booking.dto.response.BookingResponse;
import com.example.booking.dto.response.BookingStatusHistoryResponse;
import com.example.booking.dto.response.ErrorResponse;
import com.example.booking.dto.response.FlightSeatResponse;
import com.example.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "Operations for flight reservations, passenger manifest, status audits, and cancellations")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a flight reservation", description = "Validates fare with Fare Service via RabbitMQ, stores passengers, captures price snapshot, records audit trail, and generates standard PNR")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking successfully created",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Fare validation rejected",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Fare Service unavailable via RabbitMQ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Parameter(description = "Optional idempotency key to prevent duplicate booking submissions upon retries")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Parameter(description = "Optional correlation ID for distributed tracing")
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @Valid @RequestBody CreateBookingRequest request) {

        String traceId = (correlationId != null && !correlationId.isBlank())
                ? correlationId
                : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        try {
            log.info("REST request to create booking: userId={}, flightId={}, fareId={}, traceId={}, idempotencyKey={}",
                    request.getUserId(), request.getFlightId(), request.getFareId(), traceId, idempotencyKey);

            BookingResponse response = bookingService.createBooking(request, idempotencyKey);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } finally {
            MDC.remove("traceId");
        }
    }

    @Operation(summary = "Get all bookings", description = "Retrieves all reservations, optionally filtered by user ID")
    @ApiResponse(responseCode = "200", description = "List of bookings")
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings(@RequestParam(required = false) Long userId) {
        log.info("REST request to get all bookings, userId={}", userId);
        return ResponseEntity.ok(bookingService.getAllBookings(userId));
    }

    @Operation(summary = "Get booking by ID", description = "Retrieves complete booking details and passenger manifest by booking ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        log.info("REST request to get booking by id={}", bookingId);
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @Operation(summary = "Get booking by airline PNR", description = "Retrieves reservation using standard 6-character PNR (e.g. A7K92P)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/pnr/{pnr}")
    public ResponseEntity<BookingResponse> getBookingByPnr(@PathVariable String pnr) {
        log.info("REST request to get booking by PNR={}", pnr);
        return ResponseEntity.ok(bookingService.getBookingByPnr(pnr));
    }

    @Operation(summary = "Get all bookings for a user", description = "Retrieves reservation history for a specified user ID")
    @ApiResponse(responseCode = "200", description = "List of user bookings")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable Long userId) {
        log.info("REST request to get bookings for userId={}", userId);
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @Operation(summary = "Get booking status audit history", description = "Retrieves chronological status lifecycle audit records for a reservation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of status transition logs"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/{bookingId}/status")
    public ResponseEntity<List<BookingStatusHistoryResponse>> getBookingStatusHistory(@PathVariable Long bookingId) {
        log.info("REST request to get status history for bookingId={}", bookingId);
        return ResponseEntity.ok(bookingService.getBookingStatusHistory(bookingId));
    }

    @Operation(summary = "Cancel a reservation", description = "Cancels a booking, calculates cancellation fee and refund, updates status, and publishes booking.cancelled event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking successfully cancelled",
                    content = @Content(schema = @Schema(implementation = BookingCancellationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "409", description = "Booking cannot be cancelled from current state")
    })
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingCancellationResponse> cancelBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody CancelBookingRequest request) {
        log.info("REST request to cancel bookingId={}", bookingId);
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, request));
    }

    @Operation(summary = "Confirm booking (Payment Success)", description = "Transitions status from PAYMENT_PENDING to CONFIRMED")
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long bookingId) {
        log.info("REST request to confirm bookingId={}", bookingId);
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId));
    }

    @Operation(summary = "Hold a flight reservation (temporary seat hold)", description = "Temporarily locks selected seats with an expiration window (default 15 mins)")
    @PostMapping("/hold")
    public ResponseEntity<BookingResponse> holdBooking(@Valid @RequestBody HoldBookingRequest request) {
        log.info("REST request to hold booking: userId={}, flightId={}, fareId={}",
                request.getUserId(), request.getFlightId(), request.getFareId());
        BookingResponse response = bookingService.holdBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Release a held reservation", description = "Releases temporary hold on seats and cancels the booking")
    @PostMapping("/{bookingId}/release")
    public ResponseEntity<BookingResponse> releaseHold(@PathVariable Long bookingId) {
        log.info("REST request to release hold for bookingId={}", bookingId);
        return ResponseEntity.ok(bookingService.releaseBookingHold(bookingId));
    }

    @Operation(summary = "Get seat map / status for a flight", description = "Returns all seats, their cabin classes, and availability status (AVAILABLE, HELD, RESERVED, BLOCKED)")
    @GetMapping("/seats/flight/{flightId}")
    public ResponseEntity<List<FlightSeatResponse>> getFlightSeats(@PathVariable Long flightId) {
        log.info("REST request to get seats for flightId={}", flightId);
        return ResponseEntity.ok(bookingService.getFlightSeats(flightId));
    }

    @Operation(summary = "Admin: Block flight seats", description = "Blocks specific flight seats for VIP, maintenance, or airline discretion")
    @PostMapping("/admin/seats/block")
    public ResponseEntity<List<FlightSeatResponse>> adminBlockSeats(@Valid @RequestBody AdminBlockSeatsRequest request) {
        log.info("REST request to block seats: flightId={}, seats={}", request.getFlightId(), request.getSeatNumbers());
        return ResponseEntity.ok(bookingService.adminBlockSeats(request));
    }

    @Operation(summary = "Admin: Unblock flight seats", description = "Restores blocked flight seats back to AVAILABLE status")
    @PostMapping("/admin/seats/unblock")
    public ResponseEntity<List<FlightSeatResponse>> adminUnblockSeats(@Valid @RequestBody AdminUnblockSeatsRequest request) {
        log.info("REST request to unblock seats: flightId={}, seats={}", request.getFlightId(), request.getSeatNumbers());
        return ResponseEntity.ok(bookingService.adminUnblockSeats(request));
    }

    @Operation(summary = "Admin: Block a booking", description = "Blocks a reservation due to security, fraud, or administrative reasons")
    @PostMapping("/admin/{bookingId}/block")
    public ResponseEntity<BookingResponse> adminBlockBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody AdminBlockBookingRequest request) {
        log.info("REST request to block bookingId={}, reason={}", bookingId, request.getReason());
        return ResponseEntity.ok(bookingService.adminBlockBooking(bookingId, request));
    }

    @Operation(summary = "Admin: Unblock a booking", description = "Restores a blocked reservation back to active status")
    @PostMapping("/admin/{bookingId}/unblock")
    public ResponseEntity<BookingResponse> adminUnblockBooking(@PathVariable Long bookingId) {
        log.info("REST request to unblock bookingId={}", bookingId);
        return ResponseEntity.ok(bookingService.adminUnblockBooking(bookingId));
    }
}
