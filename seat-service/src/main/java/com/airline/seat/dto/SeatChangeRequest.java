package com.airline.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SeatChangeRequest {

    @NotBlank(message = "flightScheduleId is required")
    private String flightScheduleId;

    @NotNull(message = "currentSeatId is required")
    private Long currentSeatId;

    @NotNull(message = "newSeatId is required")
    private Long newSeatId;

    @NotBlank(message = "userId is required")
    private String userId;

    public SeatChangeRequest() {
    }

    public SeatChangeRequest(String flightScheduleId, Long currentSeatId, Long newSeatId, String userId) {
        this.flightScheduleId = flightScheduleId;
        this.currentSeatId = currentSeatId;
        this.newSeatId = newSeatId;
        this.userId = userId;
    }

    public String getFlightScheduleId() {
        return flightScheduleId;
    }

    public void setFlightScheduleId(String flightScheduleId) {
        this.flightScheduleId = flightScheduleId;
    }

    public Long getCurrentSeatId() {
        return currentSeatId;
    }

    public void setCurrentSeatId(Long currentSeatId) {
        this.currentSeatId = currentSeatId;
    }

    public Long getNewSeatId() {
        return newSeatId;
    }

    public void setNewSeatId(Long newSeatId) {
        this.newSeatId = newSeatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
