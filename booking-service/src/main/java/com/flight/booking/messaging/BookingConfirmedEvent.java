package com.flight.booking.messaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingConfirmedEvent implements Serializable {

    private Long bookingId;
    private String bookingReference;
    private Long userId;
    private Long flightId;
    private String passengerName;
    private String passengerEmail;
    private String seatNumber;
    private BigDecimal fareAmount;
    private Long paymentId;
    private LocalDateTime timestamp;

    public BookingConfirmedEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public BookingConfirmedEvent(Long bookingId, String bookingReference, Long userId, Long flightId,
                                 String passengerName, String passengerEmail, String seatNumber,
                                 BigDecimal fareAmount, Long paymentId) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.flightId = flightId;
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.seatNumber = seatNumber;
        this.fareAmount = fareAmount;
        this.paymentId = paymentId;
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

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
