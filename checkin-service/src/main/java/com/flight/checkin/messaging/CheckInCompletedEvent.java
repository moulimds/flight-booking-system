package com.flight.checkin.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CheckInCompletedEvent implements Serializable {

    private Long checkInId;
    private String checkInReference;
    private String bookingId;
    private String passengerId;
    private String flightId;
    private String seatNumber;
    private String boardingPassId;
    private LocalDateTime checkInTime;

    public CheckInCompletedEvent() {
        this.checkInTime = LocalDateTime.now();
    }

    public CheckInCompletedEvent(Long checkInId, String checkInReference, String bookingId, String passengerId,
                                 String flightId, String seatNumber, String boardingPassId) {
        this.checkInId = checkInId;
        this.checkInReference = checkInReference;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.boardingPassId = boardingPassId;
        this.checkInTime = LocalDateTime.now();
    }

    public Long getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(Long checkInId) {
        this.checkInId = checkInId;
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

    public String getBoardingPassId() {
        return boardingPassId;
    }

    public void setBoardingPassId(String boardingPassId) {
        this.boardingPassId = boardingPassId;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }
}
