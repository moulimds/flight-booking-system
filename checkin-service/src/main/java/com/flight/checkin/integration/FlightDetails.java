package com.flight.checkin.integration;

import java.time.LocalDateTime;

public class FlightDetails {

    private String flightId;
    private String flightNumber;
    private String origin;
    private String destination;
    private String status; 
    private LocalDateTime departureTime;
    private String gate;
    private String terminal;
    private boolean checkInWindowOpen;

    public FlightDetails() {
    }

    public FlightDetails(String flightId, String flightNumber, String origin, String destination,
                         String status, LocalDateTime departureTime, String gate, String terminal,
                         boolean checkInWindowOpen) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.status = status;
        this.departureTime = departureTime;
        this.gate = gate;
        this.terminal = terminal;
        this.checkInWindowOpen = checkInWindowOpen;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public boolean isCheckInWindowOpen() {
        return checkInWindowOpen;
    }

    public void setCheckInWindowOpen(boolean checkInWindowOpen) {
        this.checkInWindowOpen = checkInWindowOpen;
    }
}
