package com.example.fare.controller;

import com.example.fare.dto.message.FareValidationResponse;
import com.example.fare.dto.request.CalculatePriceRequest;
import com.example.fare.dto.request.CreateFareRequest;
import com.example.fare.dto.request.UpdateFareRequest;
import com.example.fare.dto.request.UpdateFareStatusRequest;
import com.example.fare.dto.request.ValidateFareRequest;
import com.example.fare.dto.response.CalculatePriceResponse;
import com.example.fare.dto.response.ErrorResponse;
import com.example.fare.dto.response.FareResponse;
import com.example.fare.entity.FareStatus;
import com.example.fare.service.FareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/fares")
@RequiredArgsConstructor
@Tag(name = "Fare Management", description = "Operations for flight fares, pricing, fare rules, and validations")
public class FareController {

    private final FareService fareService;

    @Operation(summary = "Get all fares", description = "Retrieves all flight fares, optionally filtered by flight ID and/or status")
    @ApiResponse(responseCode = "200", description = "List of fares")
    @GetMapping
    public ResponseEntity<List<FareResponse>> getAllFares(
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) FareStatus status) {
        log.info("REST request to get all fares: flightId={}, status={}", flightId, status);
        return ResponseEntity.ok(fareService.getAllFares(flightId, status));
    }

    @Operation(summary = "Calculate fare pricing", description = "Calculates total pricing for a fare given a passenger count")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calculated pricing breakdown",
                    content = @Content(schema = @Schema(implementation = CalculatePriceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fare not found")
    })
    @PostMapping("/calculate")
    public ResponseEntity<CalculatePriceResponse> calculatePrice(@Valid @RequestBody CalculatePriceRequest request) {
        log.info("REST request to calculate pricing: fareId={}, passengers={}",
                request.getFareId(), request.getPassengerCount());
        return ResponseEntity.ok(fareService.calculatePrice(request));
    }

    @Operation(summary = "Create a new flight fare", description = "Creates a new fare definition along with fare rules, cabin class, and seat quota")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fare successfully created",
                    content = @Content(schema = @Schema(implementation = FareResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Business validation failure",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<FareResponse> createFare(@Valid @RequestBody CreateFareRequest request) {
        log.info("REST request to create fare for flightId={}", request.getFlightId());
        FareResponse response = fareService.createFare(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get fare by ID", description = "Retrieves fare details including pricing and fare rules by fare ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fare found",
                    content = @Content(schema = @Schema(implementation = FareResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fare not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{fareId}")
    public ResponseEntity<FareResponse> getFareById(@PathVariable Long fareId) {
        log.info("REST request to get fare with id={}", fareId);
        return ResponseEntity.ok(fareService.getFareById(fareId));
    }

    @Operation(summary = "Get all fares for a flight", description = "Retrieves all fare options (Basic, Flexible, Premium) configured for a given flight")
    @ApiResponse(responseCode = "200", description = "List of fares")
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<FareResponse>> getFaresByFlight(@PathVariable Long flightId) {
        log.info("REST request to get all fares for flightId={}", flightId);
        return ResponseEntity.ok(fareService.getFaresByFlightId(flightId));
    }

    @Operation(summary = "Get available fares for a flight", description = "Retrieves only active, non-expired fares with available seats for a given flight")
    @ApiResponse(responseCode = "200", description = "List of available fares")
    @GetMapping("/flight/{flightId}/available")
    public ResponseEntity<List<FareResponse>> getAvailableFares(@PathVariable Long flightId) {
        log.info("REST request to get available fares for flightId={}", flightId);
        return ResponseEntity.ok(fareService.getAvailableFaresByFlightId(flightId));
    }

    @Operation(summary = "Quick validate fare by ID", description = "Performs validation check for a specific fare ID")
    @GetMapping("/{fareId}/validate")
    public ResponseEntity<FareResponse> validateFareById(@PathVariable Long fareId) {
        log.info("REST request to validate fare id={}", fareId);
        FareResponse response = fareService.getFareById(fareId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validate fare and calculate total price for booking", description = "Validates fare existence, active status, validity dates, and seat availability, calculating total price based on requested seats")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validation result",
                    content = @Content(schema = @Schema(implementation = FareValidationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PostMapping("/validate")
    public ResponseEntity<FareValidationResponse> validateFareForBooking(@Valid @RequestBody ValidateFareRequest request) {
        String requestId = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("REST request to validate fare: flightId={}, fareId={}, seats={}",
                request.getFlightId(), request.getFareId(), request.getRequiredSeats());
        FareValidationResponse response = fareService.validateFare(
                request.getFlightId(),
                request.getFareId(),
                request.getRequiredSeats(),
                requestId
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update an existing fare", description = "Updates pricing, seat availability, or rules for a fare")
    @PutMapping("/{fareId}")
    public ResponseEntity<FareResponse> updateFare(@PathVariable Long fareId,
                                                   @Valid @RequestBody UpdateFareRequest request) {
        log.info("REST request to update fare with id={}", fareId);
        return ResponseEntity.ok(fareService.updateFare(fareId, request));
    }

    @Operation(summary = "Update fare status", description = "Activates, deactivates, or marks a fare as sold out / expired")
    @PatchMapping("/{fareId}/status")
    public ResponseEntity<FareResponse> updateFareStatus(@PathVariable Long fareId,
                                                         @Valid @RequestBody UpdateFareStatusRequest request) {
        log.info("REST request to update fare status for fareId={} to {}", fareId, request.getStatus());
        return ResponseEntity.ok(fareService.updateFareStatus(fareId, request.getStatus()));
    }

    @Operation(summary = "Delete fare (soft delete)", description = "Sets fare status to INACTIVE")
    @ApiResponse(responseCode = "204", description = "Fare deactivated successfully")
    @DeleteMapping("/{fareId}")
    public ResponseEntity<Void> deleteFare(@PathVariable Long fareId) {
        log.info("REST request to delete fare with id={}", fareId);
        fareService.deleteFare(fareId);
        return ResponseEntity.noContent().build();
    }
}
