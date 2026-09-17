package com.airline.seat.controller;

import com.airline.seat.dto.*;
import com.airline.seat.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
@Tag(name = "Seat Management", description = "Endpoints for holding, reserving, releasing, blocking, changing, and viewing flight seats.")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/hold")
    @Operation(summary = "Hold seats dynamically with TTL", description = "Temporarily locks seats for a specific user during the checkout process with a configurable TTL (minutes).")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> holdSeats(@Valid @RequestBody SeatHoldRequest request) {
        List<SeatResponse> responses = seatService.holdSeats(request);
        return ResponseEntity.ok(ApiResponse.success("Seats successfully placed on hold", responses));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve seats", description = "Finalizes held seats into a confirmed reserved state linked to a confirmed booking reference.")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> reserveSeats(@Valid @RequestBody SeatReserveRequest request) {
        List<SeatResponse> responses = seatService.reserveSeats(request);
        return ResponseEntity.ok(ApiResponse.success("Seats successfully reserved", responses));
    }

    @PostMapping("/release")
    @Operation(summary = "Release held seats", description = "Explicitly releases seats previously placed on hold back to the available pool.")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> releaseSeats(@Valid @RequestBody SeatReleaseRequest request) {
        List<SeatResponse> responses = seatService.releaseSeats(request);
        return ResponseEntity.ok(ApiResponse.success("Seats successfully released back to available pool", responses));
    }

    @PostMapping("/block")
    @Operation(summary = "Block seats (Operations/Maintenance)", description = "Restricts specific seats from booking due to operational reasons (crew seating, maintenance, weight balance).")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> blockSeats(@Valid @RequestBody SeatBlockRequest request) {
        List<SeatResponse> responses = seatService.blockSeats(request);
        return ResponseEntity.ok(ApiResponse.success("Seats successfully blocked", responses));
    }

    @PostMapping("/unblock")
    @Operation(summary = "Unblock seats (Operations)", description = "Restores blocked seats back to the available pool.")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> unblockSeats(@Valid @RequestBody SeatUnblockRequest request) {
        List<SeatResponse> responses = seatService.unblockSeats(request);
        return ResponseEntity.ok(ApiResponse.success("Seats successfully unblocked", responses));
    }

    @PostMapping("/change")
    @Operation(summary = "Change assigned seat", description = "Atomically transfers a customer's reservation or hold from their current seat to a new available seat.")
    public ResponseEntity<ApiResponse<SeatResponse>> changeSeat(@Valid @RequestBody SeatChangeRequest request) {
        SeatResponse response = seatService.changeSeat(request);
        return ResponseEntity.ok(ApiResponse.success("Seat assignment successfully changed", response));
    }

    @GetMapping("/available-count")
    @Operation(summary = "Get available seat count", description = "Retrieves real-time counts and class-based breakdowns of available, held, reserved, and blocked seats for a schedule.")
    public ResponseEntity<ApiResponse<AvailableSeatsCountResponse>> getAvailableSeatsCount(
            @Parameter(description = "Flight Schedule identifier, e.g. SCH-101", required = true)
            @RequestParam String flightScheduleId) {
        AvailableSeatsCountResponse response = seatService.getAvailableSeatsCount(flightScheduleId);
        return ResponseEntity.ok(ApiResponse.success("Available seat count retrieved successfully", response));
    }

    @GetMapping("/flight/{flightId}")
    @Operation(summary = "Get flight seat map layout", description = "Fetches the complete cabin layout, seat classes, pricing multipliers, and current inventory statuses for a flight.")
    public ResponseEntity<ApiResponse<FlightSeatMapResponse>> getFlightSeatMap(
            @Parameter(description = "Flight identifier, e.g. FL-101", required = true)
            @PathVariable String flightId,
            @Parameter(description = "Optional Flight Schedule identifier, e.g. SCH-101")
            @RequestParam(required = false, defaultValue = "SCH-101") String flightScheduleId) {
        FlightSeatMapResponse response = seatService.getFlightSeatMap(flightId, flightScheduleId);
        return ResponseEntity.ok(ApiResponse.success("Flight seat map retrieved successfully", response));
    }
}
