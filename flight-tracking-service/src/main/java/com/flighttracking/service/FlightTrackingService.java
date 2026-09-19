package com.flighttracking.service;

import com.flighttracking.dto.FlightJourneyResponse;
import com.flighttracking.dto.FlightPositionResponse;
import com.flighttracking.dto.FlightTrackingResponse;

public interface FlightTrackingService {

    FlightTrackingResponse startTracking(String flightNumber);

    FlightTrackingResponse stopTracking(String flightNumber);

    FlightTrackingResponse getCurrentTracking(String flightNumber);

    FlightPositionResponse getCurrentPosition(String flightNumber);

    FlightJourneyResponse getCompleteJourney(String flightNumber);
}
