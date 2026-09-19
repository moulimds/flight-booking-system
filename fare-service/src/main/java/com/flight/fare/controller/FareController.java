package com.flight.fare.controller;

import com.flight.fare.dto.FareRequest;
import com.flight.fare.dto.FareResponse;
import com.flight.fare.service.FareService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private static final Logger logger = LoggerFactory.getLogger(FareController.class);

    @Autowired
    private FareService fareService;

    @PostMapping
    public ResponseEntity<FareResponse> createFare(@Valid @RequestBody FareRequest request) {
        logger.info("REST request to create fare for flight id: {}", request.getFlightId());
        FareResponse response = fareService.createFare(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FareResponse>> getAllFares() {
        logger.info("REST request to get all fares");
        List<FareResponse> fares = fareService.getAllFares();
        return ResponseEntity.ok(fares);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FareResponse> getFareById(@PathVariable("id") Long id) {
        logger.info("REST request to get fare with id: {}", id);
        FareResponse fare = fareService.getFareById(id);
        return ResponseEntity.ok(fare);
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<FareResponse> getFareByFlightId(@PathVariable("flightId") Long flightId) {
        logger.info("REST request to get fare for flight id: {}", flightId);
        FareResponse fare = fareService.getFareByFlightId(flightId);
        return ResponseEntity.ok(fare);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FareResponse> updateFare(@PathVariable("id") Long id, @Valid @RequestBody FareRequest request) {
        logger.info("REST request to update fare id: {}", id);
        FareResponse response = fareService.updateFare(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFare(@PathVariable("id") Long id) {
        logger.info("REST request to delete fare id: {}", id);
        fareService.deleteFare(id);
        return ResponseEntity.noContent().build();
    }
}
