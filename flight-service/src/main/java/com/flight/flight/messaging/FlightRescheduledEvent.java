package com.flight.flight.messaging;

import java.time.LocalDate;
import java.time.LocalTime;

public class FlightRescheduledEvent {

    private String eventType = "FLIGHT_RESCHEDULED";
    private Long flightId;
    private String flightNumber;
    private LocalDate oldDepartureDate;
    private LocalTime oldDepartureTime;
    private LocalDate newDepartureDate;
    private LocalTime newDepartureTime;

    public FlightRescheduledEvent() {
    }

    public FlightRescheduledEvent(Long flightId, String flightNumber, LocalDate oldDepartureDate, LocalTime oldDepartureTime, LocalDate newDepartureDate, LocalTime newDepartureTime) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.oldDepartureDate = oldDepartureDate;
        this.oldDepartureTime = oldDepartureTime;
        this.newDepartureDate = newDepartureDate;
        this.newDepartureTime = newDepartureTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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

    public LocalDate getOldDepartureDate() {
        return oldDepartureDate;
    }

    public void setOldDepartureDate(LocalDate oldDepartureDate) {
        this.oldDepartureDate = oldDepartureDate;
    }

    public LocalTime getOldDepartureTime() {
        return oldDepartureTime;
    }

    public void setOldDepartureTime(LocalTime oldDepartureTime) {
        this.oldDepartureTime = oldDepartureTime;
    }

    public LocalDate getNewDepartureDate() {
        return newDepartureDate;
    }

    public void setNewDepartureDate(LocalDate newDepartureDate) {
        this.newDepartureDate = newDepartureDate;
    }

    public LocalTime getNewDepartureTime() {
        return newDepartureTime;
    }

    public void setNewDepartureTime(LocalTime newDepartureTime) {
        this.newDepartureTime = newDepartureTime;
    }
}
