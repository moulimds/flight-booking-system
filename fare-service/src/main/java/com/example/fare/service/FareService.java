package com.example.fare.service;

import com.example.fare.dto.message.FareValidationResponse;
import com.example.fare.dto.request.CreateFareRequest;
import com.example.fare.dto.request.UpdateFareRequest;
import com.example.fare.dto.response.FareResponse;
import com.example.fare.entity.FareStatus;

import java.util.List;

public interface FareService {

    FareResponse createFare(CreateFareRequest request);

    FareResponse getFareById(Long fareId);

    List<FareResponse> getFaresByFlightId(Long flightId);

    List<FareResponse> getAvailableFaresByFlightId(Long flightId);

    FareResponse updateFare(Long fareId, UpdateFareRequest request);

    FareResponse updateFareStatus(Long fareId, FareStatus status);

    void deleteFare(Long fareId);

    List<FareResponse> getAllFares(Long flightId, FareStatus status);

    com.example.fare.dto.response.CalculatePriceResponse calculatePrice(com.example.fare.dto.request.CalculatePriceRequest request);

    FareValidationResponse validateFare(Long flightId, Long fareId, Integer requiredSeats, String requestId);
}
