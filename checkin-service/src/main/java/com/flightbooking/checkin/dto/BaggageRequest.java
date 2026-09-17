package com.flightbooking.checkin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BaggageRequest(
    @NotNull @Min(0) Integer baggageCount,
    @NotNull @Min(0) Double baggageWeight
) {}
