package com.flight.seat.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SeatHeldEvent implements Serializable {

    private Long seatId;
    private Long flightId;
    private String seatNumber;
    private Long userId;
    private LocalDateTime timestamp;

    public SeatHeldEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public SeatHeldEvent(Long seatId, Long flightId, String seatNumber, Long userId) {
        this.seatId = seatId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
