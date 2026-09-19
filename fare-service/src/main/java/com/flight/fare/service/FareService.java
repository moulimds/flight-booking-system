package com.flight.fare.service;

import com.flight.fare.dto.FareRequest;
import com.flight.fare.dto.FareResponse;

import java.util.List;

public interface FareService {
    FareResponse createFare(FareRequest request);
    List<FareResponse> getAllFares();
    FareResponse getFareById(Long id);
    FareResponse getFareByFlightId(Long flightId);
    FareResponse updateFare(Long id, FareRequest request);
    void deleteFare(Long id);
}
