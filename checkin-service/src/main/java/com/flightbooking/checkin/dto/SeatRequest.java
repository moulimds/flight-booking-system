package com.flightbooking.checkin.dto;

import jakarta.validation.constraints.NotBlank;

public record SeatRequest(@NotBlank String seatNumber) {}
