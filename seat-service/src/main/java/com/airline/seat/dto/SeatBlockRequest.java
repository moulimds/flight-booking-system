package com.airline.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SeatBlockRequest {

    @NotBlank(message = "flightScheduleId is required")
    private String flightScheduleId;

    @NotEmpty(message = "seatIds cannot be empty")
    private List<Long> seatIds;

    @NotBlank(message = "reason is required")
    private String reason;

    @NotBlank(message = "blockedBy is required")
    private String blockedBy;

    public SeatBlockRequest() {
    }

    public SeatBlockRequest(String flightScheduleId, List<Long> seatIds, String reason, String blockedBy) {
        this.flightScheduleId = flightScheduleId;
        this.seatIds = seatIds;
        this.reason = reason;
        this.blockedBy = blockedBy;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(String blockedBy) {
        this.blockedBy = blockedBy;
    }
}
