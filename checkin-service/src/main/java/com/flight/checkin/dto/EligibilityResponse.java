package com.flight.checkin.dto;

public class EligibilityResponse {

    private boolean eligible;
    private String bookingId;
    private String passengerId;
    private String flightId;
    private String message;

    public EligibilityResponse() {
    }

    public EligibilityResponse(boolean eligible, String bookingId, String passengerId, String flightId, String message) {
        this.eligible = eligible;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.flightId = flightId;
        this.message = message;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
