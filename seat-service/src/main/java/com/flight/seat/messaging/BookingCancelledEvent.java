package com.flight.seat.messaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingCancelledEvent implements Serializable {

    private Long bookingId;
    private String bookingReference;
    private Long flightId;
    private String seatNumber;
    private Long userId;
    private BigDecimal fareAmount;
    private LocalDateTime timestamp;

    public BookingCancelledEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public BookingCancelledEvent(Long bookingId, String bookingReference, Long flightId, String seatNumber, Long userId, BigDecimal fareAmount) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.userId = userId;
        this.fareAmount = fareAmount;
        this.timestamp = LocalDateTime.now();
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
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

    public BigDecimal getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(BigDecimal fareAmount) {
        this.fareAmount = fareAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
