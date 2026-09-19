package com.flight.seat.dto;

public class SeatHoldRequest {

    private Long userId;
    private Long flightId;
    private String seatNumber;
    private Long seatId;

    public SeatHoldRequest() {
    }

    public SeatHoldRequest(Long userId) {
        this.userId = userId;
    }

    public SeatHoldRequest(Long userId, Long flightId, String seatNumber) {
        this.userId = userId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }
}
