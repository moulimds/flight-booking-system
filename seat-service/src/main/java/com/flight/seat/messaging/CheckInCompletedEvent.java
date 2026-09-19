package com.flight.seat.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CheckInCompletedEvent implements Serializable {

    private Long checkInId;
    private String checkInReference;
    private Long bookingId;
    private String passengerName;
    private Long flightId;
    private String seatNumber;
    private String boardingPassNumber;
    private LocalDateTime checkInTime;

    public CheckInCompletedEvent() {
        this.checkInTime = LocalDateTime.now();
    }

    public CheckInCompletedEvent(Long checkInId, String checkInReference, Long bookingId, String passengerName,
                                 Long flightId, String seatNumber, String boardingPassNumber) {
        this.checkInId = checkInId;
        this.checkInReference = checkInReference;
        this.bookingId = bookingId;
        this.passengerName = passengerName;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.boardingPassNumber = boardingPassNumber;
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

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
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

    public String getBoardingPassNumber() {
        return boardingPassNumber;
    }

    public void setBoardingPassNumber(String boardingPassNumber) {
        this.boardingPassNumber = boardingPassNumber;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }
}
