package com.flight.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SeatLockRequest {

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotBlank(message = "Seat number is required")
    private String seatNumber;

    @NotNull(message = "User ID is required")
    private Long userId;

    private Integer ttlSeconds = 600;

    public SeatLockRequest() {}

    public SeatLockRequest(Long flightId, String seatNumber, Long userId, Integer ttlSeconds) {
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.userId = userId;
        this.ttlSeconds = ttlSeconds != null ? ttlSeconds : 600;
    }

    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getTtlSeconds() { return ttlSeconds; }
    public void setTtlSeconds(Integer ttlSeconds) { this.ttlSeconds = ttlSeconds; }
}