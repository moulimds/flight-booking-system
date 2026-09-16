package com.airline.seat.dto;

import java.util.List;

public class FlightSeatMapResponse {

    private String flightId;
    private String flightScheduleId;
    private int totalSeats;
    private List<SeatResponse> seats;

    public FlightSeatMapResponse() {
    }

    public FlightSeatMapResponse(String flightId, String flightScheduleId, int totalSeats, List<SeatResponse> seats) {
        this.flightId = flightId;
        this.flightScheduleId = flightScheduleId;
        this.totalSeats = totalSeats;
        this.seats = seats;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getFlightScheduleId() {
        return flightScheduleId;
    }

    public void setFlightScheduleId(String flightScheduleId) {
        this.flightScheduleId = flightScheduleId;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public List<SeatResponse> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatResponse> seats) {
        this.seats = seats;
    }
}
