package com.airline.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SeatUnblockRequest {

    @NotBlank(message = "flightScheduleId is required")
    private String flightScheduleId;

    @NotEmpty(message = "seatIds cannot be empty")
    private List<Long> seatIds;

    public SeatUnblockRequest() {
    }

    public SeatUnblockRequest(String flightScheduleId, List<Long> seatIds) {
        this.flightScheduleId = flightScheduleId;
        this.seatIds = seatIds;
    }

    public String getFlightScheduleId() {
        return flightScheduleId;
    }

    public void setFlightScheduleId(String flightScheduleId) {
        this.flightScheduleId = flightScheduleId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }
}
