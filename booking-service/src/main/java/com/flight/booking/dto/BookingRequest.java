package com.flight.booking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Flight ID is required")
    private Long flightId;

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

    @PositiveOrZero(message = "Fare amount cannot be negative")
    private BigDecimal fareAmount;

    private java.util.List<PassengerRequest> passengers;

    @AssertTrue(message = "Passenger name is required")
    public boolean isPassengerNameValid() {
        return getPassengerName() != null && !getPassengerName().isBlank();
    }

    @AssertTrue(message = "Passenger email is required")
    public boolean isPassengerEmailValid() {
        return getPassengerEmail() != null && !getPassengerEmail().isBlank();
    }

    @AssertTrue(message = "Passenger phone is required")
    public boolean isPassengerPhoneValid() {
        return getPassengerPhone() != null && !getPassengerPhone().isBlank();
    }

    @AssertTrue(message = "Seat number is required")
    public boolean isSeatNumberValid() {
        return getSeatNumber() != null && !getSeatNumber().isBlank();
    }

    public BookingRequest() {
    }

    public BookingRequest(Long userId, Long flightId, String passengerName, String passengerEmail,
                          String passengerPhone, LocalDate passengerDateOfBirth, String passengerGender,
                          String seatNumber, BigDecimal fareAmount) {
        this.userId = userId;
        this.flightId = flightId;
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.passengerPhone = passengerPhone;
        this.passengerDateOfBirth = passengerDateOfBirth;
        this.passengerGender = passengerGender;
        this.seatNumber = seatNumber;
        this.fareAmount = fareAmount;
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
        if (passengerName != null) return passengerName;
        if (passengers != null && !passengers.isEmpty() && passengers.get(0).getPassengerName() != null) {
            return passengers.get(0).getPassengerName();
        }
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerEmail() {
        if (passengerEmail != null) return passengerEmail;
        if (passengers != null && !passengers.isEmpty() && passengers.get(0).getPassengerEmail() != null) {
            return passengers.get(0).getPassengerEmail();
        }
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getPassengerPhone() {
        if (passengerPhone != null) return passengerPhone;
        if (passengers != null && !passengers.isEmpty() && passengers.get(0).getPassengerPhone() != null) {
            return passengers.get(0).getPassengerPhone();
        }
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public LocalDate getPassengerDateOfBirth() {
        if (passengerDateOfBirth != null) return passengerDateOfBirth;
        if (passengers != null && !passengers.isEmpty() && passengers.get(0).getPassengerDateOfBirth() != null) {
            return passengers.get(0).getPassengerDateOfBirth();
        }
        return passengerDateOfBirth;
    }

    public void setPassengerDateOfBirth(LocalDate passengerDateOfBirth) {
        this.passengerDateOfBirth = passengerDateOfBirth;
    }

    public String getPassengerGender() {
        if (passengerGender != null) return passengerGender;
        if (passengers != null && !passengers.isEmpty() && passengers.get(0).getPassengerGender() != null) {
            return passengers.get(0).getPassengerGender();
        }
        return passengerGender;
    }

    public void setPassengerGender(String passengerGender) {
        this.passengerGender = passengerGender;
    }

    public String getSeatNumber() {
        if (seatNumber != null) return seatNumber;
        if (passengers != null && !passengers.isEmpty() && passengers.get(0).getSeatNumber() != null) {
            return passengers.get(0).getSeatNumber();
        }
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

    public java.util.List<PassengerRequest> getPassengers() {
        return passengers;
    }

    public void setPassengers(java.util.List<PassengerRequest> passengers) {
        this.passengers = passengers;
        if (passengers != null && !passengers.isEmpty()) {
            PassengerRequest first = passengers.get(0);
            if (this.passengerName == null) this.passengerName = first.getPassengerName();
            if (this.passengerEmail == null) this.passengerEmail = first.getPassengerEmail();
            if (this.passengerPhone == null) this.passengerPhone = first.getPassengerPhone();
            if (this.seatNumber == null) this.seatNumber = first.getSeatNumber();
            if (this.passengerDateOfBirth == null) this.passengerDateOfBirth = first.getPassengerDateOfBirth();
            if (this.passengerGender == null) this.passengerGender = first.getPassengerGender();
        }
    }
}
