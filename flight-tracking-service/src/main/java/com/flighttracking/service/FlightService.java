package com.flighttracking.service;

import com.flighttracking.dto.CreateFlightRequest;
import com.flighttracking.dto.FlightResponse;

import java.util.List;

public interface FlightService {

    FlightResponse createFlight(CreateFlightRequest request);

    List<FlightResponse> getAllFlights();

    FlightResponse getFlightByFlightNumber(String flightNumber);
}
