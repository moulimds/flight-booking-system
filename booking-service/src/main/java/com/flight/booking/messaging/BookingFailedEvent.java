package com.flight.booking.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BookingFailedEvent implements Serializable {

    private Long bookingId;
    private String bookingReference;
    private Long userId;
    private String reason;
    private LocalDateTime timestamp;

    public BookingFailedEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public BookingFailedEvent(Long bookingId, String bookingReference, Long userId, String reason) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.reason = reason;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
