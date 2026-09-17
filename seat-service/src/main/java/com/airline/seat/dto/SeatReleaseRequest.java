package com.airline.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SeatReleaseRequest {

    @NotBlank(message = "flightScheduleId is required")
    private String flightScheduleId;

    @NotEmpty(message = "seatIds cannot be empty")
    private List<Long> seatIds;

    @NotBlank(message = "userId is required")
    private String userId;

    public SeatReleaseRequest() {
    }

    public SeatReleaseRequest(String flightScheduleId, List<Long> seatIds, String userId) {
        this.flightScheduleId = flightScheduleId;
        this.seatIds = seatIds;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
