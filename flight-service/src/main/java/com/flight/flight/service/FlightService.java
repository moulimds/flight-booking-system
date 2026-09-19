package com.flight.flight.service;

import com.flight.flight.dto.FlightRequest;
import com.flight.flight.dto.FlightResponse;
import com.flight.flight.dto.FlightStatusUpdateRequest;
import com.flight.flight.dto.FlightSeatsUpdateRequest;

import java.time.LocalDate;
import java.util.List;

public interface FlightService {
    FlightResponse createFlight(FlightRequest request);
    List<FlightResponse> getAllFlights();
    FlightResponse getFlightById(Long id);
    FlightResponse getFlightByFlightNumber(String flightNumber);
    FlightResponse updateFlight(Long id, FlightRequest request);
    void deleteFlight(Long id);
    List<FlightResponse> getFlightsBySource(String source);
    List<FlightResponse> getFlightsByDestination(String destination);
    List<FlightResponse> searchFlights(String source, String destination, LocalDate departureDate);
    FlightResponse updateFlightStatus(Long id, FlightStatusUpdateRequest request);
    FlightResponse updateAvailableSeats(Long id, FlightSeatsUpdateRequest request);
}
