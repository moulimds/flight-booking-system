package com.flight.seat.dto;

import jakarta.validation.constraints.NotNull;

public class SeatBookRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private Long flightId;
    private String seatNumber;
    private Long seatId;

    public SeatBookRequest() {
    }

    public SeatBookRequest(Long bookingId) {
        this.bookingId = bookingId;
    }

    public SeatBookRequest(Long bookingId, Long flightId, String seatNumber) {
        this.bookingId = bookingId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
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
