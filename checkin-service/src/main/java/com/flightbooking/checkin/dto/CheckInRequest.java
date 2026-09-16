package com.flightbooking.checkin.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(
    @NotBlank String bookingId,
    @NotBlank String passengerId,
    @NotBlank String flightId,
    String seatNumber,
    Integer baggageCount,
    Double baggageWeight
) {}
