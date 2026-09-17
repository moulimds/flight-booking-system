package com.flight.flight.dto;

import jakarta.validation.constraints.NotNull;

public class FlightSeatsUpdateRequest {

    @NotNull(message = "Available seats count is required")
    private Integer availableSeats;

    public FlightSeatsUpdateRequest() {
    }

    public FlightSeatsUpdateRequest(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }
}
