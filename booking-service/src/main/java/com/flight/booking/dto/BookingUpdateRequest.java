package com.flight.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class BookingUpdateRequest {

    private String passengerName;
    private String passengerEmail;
    private String passengerPhone;
    private LocalDate passengerDateOfBirth;
    private String passengerGender;
    private Long flightId;
    private String flightNumber;
    private String seatNumber;
    private String departureTime;

    public BookingUpdateRequest() {
    }

    public BookingUpdateRequest(String passengerName, String passengerEmail, String passengerPhone,
                                LocalDate passengerDateOfBirth, String passengerGender) {
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.passengerPhone = passengerPhone;
        this.passengerDateOfBirth = passengerDateOfBirth;
        this.passengerGender = passengerGender;
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

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public LocalDate getPassengerDateOfBirth() {
        return passengerDateOfBirth;
    }

    public void setPassengerDateOfBirth(LocalDate passengerDateOfBirth) {
        this.passengerDateOfBirth = passengerDateOfBirth;
    }

    public String getPassengerGender() {
        return passengerGender;
    }

    public void setPassengerGender(String passengerGender) {
        this.passengerGender = passengerGender;
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

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }
}
