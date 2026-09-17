package com.flight.search.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FlightDeletedEvent implements Serializable {

    private Long flightId;
    private String flightNumber;
    private LocalDateTime timestamp;

    public FlightDeletedEvent() {
    }

    public FlightDeletedEvent(Long flightId, String flightNumber) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
