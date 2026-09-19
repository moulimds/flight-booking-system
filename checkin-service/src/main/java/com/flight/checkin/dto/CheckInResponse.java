package com.flight.checkin.dto;

import java.time.LocalDateTime;

public class CheckInResponse {

    private Long id;
    private String checkInReference;
    private String bookingId;
    private String passengerId;
    private String flightId;
    private String seatNumber;
    private Integer baggageCount;
    private Double baggageWeight;
    private String status;
    private LocalDateTime checkedInAt;
    private String boardingPassId;

    public CheckInResponse() {
    }

    public CheckInResponse(Long id, String checkInReference, String bookingId, String passengerId,
                           String flightId, String seatNumber, Integer baggageCount, Double baggageWeight,
                           String status, LocalDateTime checkedInAt, String boardingPassId) {
        this.id = id;
        this.checkInReference = checkInReference;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.baggageCount = baggageCount;
        this.baggageWeight = baggageWeight;
        this.status = status;
        this.checkedInAt = checkedInAt;
        this.boardingPassId = boardingPassId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCheckInReference() {
        return checkInReference;
    }

    public void setCheckInReference(String checkInReference) {
        this.checkInReference = checkInReference;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(LocalDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public String getBoardingPassId() {
        return boardingPassId;
    }

    public void setBoardingPassId(String boardingPassId) {
        this.boardingPassId = boardingPassId;
    }
}
