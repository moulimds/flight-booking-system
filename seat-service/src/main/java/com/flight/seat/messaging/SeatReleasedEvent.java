package com.flight.seat.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SeatReleasedEvent implements Serializable {

    private Long seatId;
    private Long flightId;
    private String seatNumber;
    private Long bookingId;
    private LocalDateTime timestamp;

    public SeatReleasedEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public SeatReleasedEvent(Long seatId, Long flightId, String seatNumber) {
        this(seatId, flightId, seatNumber, null);
    }

    public SeatReleasedEvent(Long seatId, Long flightId, String seatNumber, Long bookingId) {
        this.seatId = seatId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.bookingId = bookingId;
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

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
