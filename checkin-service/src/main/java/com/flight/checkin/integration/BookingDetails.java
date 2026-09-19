package com.flight.checkin.integration;

public class BookingDetails {

    private String bookingId;
    private String passengerId;
    private String passengerName;
    private String flightId;
    private String customerEmail;
    private String bookingStatus; 
    private String paymentStatus; 
    private String seatNumber;

    public BookingDetails() {
    }

    public BookingDetails(String bookingId, String passengerId, String passengerName, String flightId,
                          String customerEmail, String bookingStatus, String paymentStatus, String seatNumber) {
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.flightId = flightId;
        this.customerEmail = customerEmail;
        this.bookingStatus = bookingStatus;
        this.paymentStatus = paymentStatus;
        this.seatNumber = seatNumber;
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

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
