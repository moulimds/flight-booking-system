package com.flight.checkin.dto;

import jakarta.validation.constraints.NotBlank;

public class SeatChangeRequest {

    private String seatNumber;

    public SeatChangeRequest() {
    }

    public SeatChangeRequest(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
