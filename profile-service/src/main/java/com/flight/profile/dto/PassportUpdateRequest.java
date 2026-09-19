package com.flight.profile.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class PassportUpdateRequest {

    @NotBlank(message = "Passport number is required")
    private String passportNumber;

    private LocalDate passportExpiryDate;
    private String passportIssuingCountry;

    public PassportUpdateRequest() {
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public LocalDate getPassportExpiryDate() {
        return passportExpiryDate;
    }

    public void setPassportExpiryDate(LocalDate passportExpiryDate) {
        this.passportExpiryDate = passportExpiryDate;
    }

    public String getPassportIssuingCountry() {
        return passportIssuingCountry;
    }

    public void setPassportIssuingCountry(String passportIssuingCountry) {
        this.passportIssuingCountry = passportIssuingCountry;
    }
}
