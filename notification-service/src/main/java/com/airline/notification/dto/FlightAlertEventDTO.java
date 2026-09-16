package com.airline.notification.dto;

import java.io.Serializable;
import java.util.List;

public class FlightAlertEventDTO implements Serializable {

    private String flightNumber;
    private String flightDate;
    private String origin;
    private String destination;
    private String airline;
    private List<String> affectedUserEmails;
    private List<String> affectedUserPhones;
    private String scheduledDepartureTime;
    private String estimatedDepartureTime;
    private String delayReason;
    private String gateNumber;
    private String alertType; // DELAYED, CANCELLED, GATE_CHANGE

    public FlightAlertEventDTO() {
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(String flightDate) {
        this.flightDate = flightDate;
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

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public List<String> getAffectedUserEmails() {
        return affectedUserEmails;
    }

    public void setAffectedUserEmails(List<String> affectedUserEmails) {
        this.affectedUserEmails = affectedUserEmails;
    }

    public List<String> getAffectedUserPhones() {
        return affectedUserPhones;
    }

    public void setAffectedUserPhones(List<String> affectedUserPhones) {
        this.affectedUserPhones = affectedUserPhones;
    }

    public String getScheduledDepartureTime() {
        return scheduledDepartureTime;
    }

    public void setScheduledDepartureTime(String scheduledDepartureTime) {
        this.scheduledDepartureTime = scheduledDepartureTime;
    }

    public String getEstimatedDepartureTime() {
        return estimatedDepartureTime;
    }

    public void setEstimatedDepartureTime(String estimatedDepartureTime) {
        this.estimatedDepartureTime = estimatedDepartureTime;
    }

    public String getDelayReason() {
        return delayReason;
    }

    public void setDelayReason(String delayReason) {
        this.delayReason = delayReason;
    }

    public String getGateNumber() {
        return gateNumber;
    }

    public void setGateNumber(String gateNumber) {
        this.gateNumber = gateNumber;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }
}
