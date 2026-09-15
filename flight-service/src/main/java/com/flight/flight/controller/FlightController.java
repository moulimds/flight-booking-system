package com.flight.flight.controller;

import com.flight.flight.dto.FlightRequest;
import com.flight.flight.dto.FlightResponse;
import com.flight.flight.dto.FlightSeatsUpdateRequest;
import com.flight.flight.dto.FlightStatusUpdateRequest;
import com.flight.flight.service.FlightService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);

    @Autowired
    private FlightService flightService;

    @PostMapping
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody FlightRequest request) {
        logger.info("REST request to create flight: {}", request.getFlightNumber());
        FlightResponse response = flightService.createFlight(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FlightResponse>> getAllFlights() {
        logger.info("REST request to get all flights");
        List<FlightResponse> flights = flightService.getAllFlights();
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable("id") Long id) {
        logger.info("REST request to get flight by id: {}", id);
        FlightResponse flight = flightService.getFlightById(id);
        return ResponseEntity.ok(flight);
    }

    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<FlightResponse> getFlightByNumber(@PathVariable("flightNumber") String flightNumber) {
        logger.info("REST request to get flight by number: {}", flightNumber);
        FlightResponse flight = flightService.getFlightByFlightNumber(flightNumber);
        return ResponseEntity.ok(flight);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightResponse> updateFlight(@PathVariable("id") Long id, @Valid @RequestBody FlightRequest request) {
        logger.info("REST request to update flight id: {}", id);
        FlightResponse response = flightService.updateFlight(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable("id") Long id) {
        logger.info("REST request to delete flight id: {}", id);
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/source/{source}")
    public ResponseEntity<List<FlightResponse>> getFlightsBySource(@PathVariable("source") String source) {
        logger.info("REST request to get flights from source: {}", source);
        List<FlightResponse> flights = flightService.getFlightsBySource(source);
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<FlightResponse>> getFlightsByDestination(@PathVariable("destination") String destination) {
        logger.info("REST request to get flights to destination: {}", destination);
        List<FlightResponse> flights = flightService.getFlightsByDestination(destination);
        return ResponseEntity.ok(flights);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchFlights(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.info("REST request to search flights: source={}, destination={}, date={}", source, destination, date);
        List<FlightResponse> flights = flightService.searchFlights(source, destination, date);
        return ResponseEntity.ok(flights);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FlightResponse> updateFlightStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody FlightStatusUpdateRequest request) {
        logger.info("REST request to update status for flight id {}: {}", id, request.getStatus());
        FlightResponse flight = flightService.updateFlightStatus(id, request);
        return ResponseEntity.ok(flight);
    }

    @PatchMapping("/{id}/seats")
    public ResponseEntity<FlightResponse> updateAvailableSeats(
            @PathVariable("id") Long id,
            @Valid @RequestBody FlightSeatsUpdateRequest request) {
        logger.info("REST request to update available seats for flight id {}: {}", id, request.getAvailableSeats());
        FlightResponse flight = flightService.updateAvailableSeats(id, request);
        return ResponseEntity.ok(flight);
    }
}
