package com.flight.booking.messaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingRescheduledEvent implements Serializable {

    private Long bookingId;
    private String bookingReference;
    private Long userId;
    private Long flightId;
    private String flightNumber;
    private String passengerName;
    private String passengerEmail;
    private String seatNumber;
    private BigDecimal fareAmount;
    private LocalDateTime rescheduledDateTime;
    private LocalDateTime timestamp;

    public BookingRescheduledEvent() {
        this.timestamp = LocalDateTime.now();
        this.rescheduledDateTime = LocalDateTime.now();
    }

    public BookingRescheduledEvent(Long bookingId, String bookingReference, Long userId, Long flightId,
                                   String flightNumber, String passengerName, String passengerEmail,
                                   String seatNumber, BigDecimal fareAmount) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.seatNumber = seatNumber;
        this.fareAmount = fareAmount;
        this.rescheduledDateTime = LocalDateTime.now();
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

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public BigDecimal getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(BigDecimal fareAmount) {
        this.fareAmount = fareAmount;
    }

    public LocalDateTime getRescheduledDateTime() {
        return rescheduledDateTime;
    }

    public void setRescheduledDateTime(LocalDateTime rescheduledDateTime) {
        this.rescheduledDateTime = rescheduledDateTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
