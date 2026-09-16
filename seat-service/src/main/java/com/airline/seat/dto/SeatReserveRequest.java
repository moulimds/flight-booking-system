package com.airline.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SeatReserveRequest {

    @NotBlank(message = "flightScheduleId is required")
    private String flightScheduleId;

    @NotEmpty(message = "seatIds cannot be empty")
    private List<Long> seatIds;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "bookingReference is required")
    private String bookingReference;

    public SeatReserveRequest() {
    }

    public SeatReserveRequest(String flightScheduleId, List<Long> seatIds, String userId, String bookingReference) {
        this.flightScheduleId = flightScheduleId;
        this.seatIds = seatIds;
        this.userId = userId;
        this.bookingReference = bookingReference;
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

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }
}
