package com.flight.checkin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CheckInRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotBlank(message = "Passenger ID is required")
    private String passengerId;

    @NotBlank(message = "Flight ID is required")
    private String flightId;

    @NotBlank(message = "Seat number is required")
    private String seatNumber;

    @NotNull(message = "Baggage count is required")
    @Min(value = 0, message = "Baggage count cannot be negative")
    private Integer baggageCount = 0;

    @NotNull(message = "Baggage weight is required")
    @DecimalMin(value = "0.0", message = "Baggage weight cannot be negative")
    private Double baggageWeight = 0.0;

    public CheckInRequest() {
    }

    public CheckInRequest(String bookingId, String passengerId, String flightId, String seatNumber,
                          Integer baggageCount, Double baggageWeight) {
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.baggageCount = baggageCount;
        this.baggageWeight = baggageWeight;
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

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getBaggageCount() {
        return baggageCount;
    }

    public void setBaggageCount(Integer baggageCount) {
        this.baggageCount = baggageCount;
    }

    public Double getBaggageWeight() {
        return baggageWeight;
    }

    public void setBaggageWeight(Double baggageWeight) {
        this.baggageWeight = baggageWeight;
    }
}
