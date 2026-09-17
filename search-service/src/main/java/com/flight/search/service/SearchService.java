package com.flight.search.service;

import com.flight.search.dto.FlightSearchResult;

import java.time.LocalDate;
import java.util.List;

public interface SearchService {
    List<FlightSearchResult> searchFlights(String source, String destination, LocalDate departureDate);
    void syncFlightData();
    void updateOrIndexFlight(Long flightId);
    void removeFlightFromIndex(Long flightId);
}
