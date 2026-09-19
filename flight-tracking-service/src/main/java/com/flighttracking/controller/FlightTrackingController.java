package com.flighttracking.controller;

import com.flighttracking.dto.FlightJourneyResponse;
import com.flighttracking.dto.FlightPositionResponse;
import com.flighttracking.dto.FlightTrackingResponse;
import com.flighttracking.service.FlightTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking/flights")
@RequiredArgsConstructor
public class FlightTrackingController {

    private final FlightTrackingService flightTrackingService;

    @PostMapping("/{flightNumber}/start")
    public ResponseEntity<FlightTrackingResponse> startTracking(@PathVariable String flightNumber) {
        FlightTrackingResponse tracking = flightTrackingService.startTracking(flightNumber);
        return ResponseEntity.ok(tracking);
    }

    @PostMapping("/{flightNumber}/stop")
    public ResponseEntity<FlightTrackingResponse> stopTracking(@PathVariable String flightNumber) {
        FlightTrackingResponse tracking = flightTrackingService.stopTracking(flightNumber);
        return ResponseEntity.ok(tracking);
    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<FlightTrackingResponse> getCurrentTracking(@PathVariable String flightNumber) {
        FlightTrackingResponse tracking = flightTrackingService.getCurrentTracking(flightNumber);
        return ResponseEntity.ok(tracking);
    }

    @GetMapping("/{flightNumber}/position")
    public ResponseEntity<FlightPositionResponse> getCurrentPosition(@PathVariable String flightNumber) {
        FlightPositionResponse position = flightTrackingService.getCurrentPosition(flightNumber);
        return ResponseEntity.ok(position);
    }

    @GetMapping("/{flightNumber}/journey")
    public ResponseEntity<FlightJourneyResponse> getCompleteJourney(@PathVariable String flightNumber) {
        FlightJourneyResponse journey = flightTrackingService.getCompleteJourney(flightNumber);
        return ResponseEntity.ok(journey);
    }
}
