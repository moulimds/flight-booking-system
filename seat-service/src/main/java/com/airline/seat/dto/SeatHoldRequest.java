package com.airline.seat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SeatHoldRequest {

    @NotBlank(message = "flightScheduleId is required")
    private String flightScheduleId;

    @NotEmpty(message = "seatIds cannot be empty")
    private List<Long> seatIds;

    @NotBlank(message = "userId is required")
    private String userId;

    @Min(value = 1, message = "Hold duration must be at least 1 minute")
    @Max(value = 60, message = "Hold duration cannot exceed 60 minutes")
    private Integer holdDurationMinutes = 10;

    public SeatHoldRequest() {
    }

    public SeatHoldRequest(String flightScheduleId, List<Long> seatIds, String userId, Integer holdDurationMinutes) {
        this.flightScheduleId = flightScheduleId;
        this.seatIds = seatIds;
        this.userId = userId;
        this.holdDurationMinutes = holdDurationMinutes != null ? holdDurationMinutes : 10;
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

    public Integer getHoldDurationMinutes() {
        return holdDurationMinutes;
    }

    public void setHoldDurationMinutes(Integer holdDurationMinutes) {
        this.holdDurationMinutes = holdDurationMinutes;
    }
}
