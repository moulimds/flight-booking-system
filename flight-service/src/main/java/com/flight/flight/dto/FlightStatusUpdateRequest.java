package com.flight.flight.dto;

import jakarta.validation.constraints.NotBlank;

public class FlightStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status; // SCHEDULED, DELAYED, CANCELLED, COMPLETED

    public FlightStatusUpdateRequest() {
    }

    public FlightStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
