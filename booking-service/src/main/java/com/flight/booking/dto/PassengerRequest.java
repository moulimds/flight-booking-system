package com.flight.booking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDate;

public class PassengerRequest {
    @JsonAlias({"passengerName", "name"})
    private String passengerName;

    @JsonAlias({"passengerEmail", "email"})
    private String passengerEmail;

    @JsonAlias({"passengerPhone", "phone"})
    private String passengerPhone;

    @JsonAlias({"passengerDateOfBirth", "dateOfBirth"})
    private LocalDate passengerDateOfBirth;

    @JsonAlias({"passengerGender", "gender"})
    private String passengerGender;

    private String seatNumber;

    public PassengerRequest() {
    }

    public PassengerRequest(String passengerName, String passengerEmail, String passengerPhone,
                            LocalDate passengerDateOfBirth, String passengerGender, String seatNumber) {
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.passengerPhone = passengerPhone;
        this.passengerDateOfBirth = passengerDateOfBirth;
        this.passengerGender = passengerGender;
        this.seatNumber = seatNumber;
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

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
