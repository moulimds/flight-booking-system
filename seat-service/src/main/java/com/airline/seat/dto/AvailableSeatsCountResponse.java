package com.airline.seat.dto;

import com.airline.seat.entity.SeatClass;
import java.util.Map;

public class AvailableSeatsCountResponse {

    private String flightScheduleId;
    private long totalSeats;
    private long availableSeats;
    private long heldSeats;
    private long reservedSeats;
    private long blockedSeats;
    private Map<SeatClass, Long> availableBreakdownByClass;

    public AvailableSeatsCountResponse() {
    }

    public AvailableSeatsCountResponse(String flightScheduleId, long totalSeats, long availableSeats, long heldSeats, long reservedSeats, long blockedSeats, Map<SeatClass, Long> availableBreakdownByClass) {
        this.flightScheduleId = flightScheduleId;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.heldSeats = heldSeats;
        this.reservedSeats = reservedSeats;
        this.blockedSeats = blockedSeats;
        this.availableBreakdownByClass = availableBreakdownByClass;
    }

    public String getFlightScheduleId() {
        return flightScheduleId;
    }

    public void setFlightScheduleId(String flightScheduleId) {
        this.flightScheduleId = flightScheduleId;
    }

    public long getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(long totalSeats) {
        this.totalSeats = totalSeats;
    }

    public long getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(long availableSeats) {
        this.availableSeats = availableSeats;
    }

    public long getHeldSeats() {
        return heldSeats;
    }

    public void setHeldSeats(long heldSeats) {
        this.heldSeats = heldSeats;
    }

    public long getReservedSeats() {
        return reservedSeats;
    }

    public void setReservedSeats(long reservedSeats) {
        this.reservedSeats = reservedSeats;
    }

    public long getBlockedSeats() {
        return blockedSeats;
    }

    public void setBlockedSeats(long blockedSeats) {
        this.blockedSeats = blockedSeats;
    }

    public Map<SeatClass, Long> getAvailableBreakdownByClass() {
        return availableBreakdownByClass;
    }

    public void setAvailableBreakdownByClass(Map<SeatClass, Long> availableBreakdownByClass) {
        this.availableBreakdownByClass = availableBreakdownByClass;
    }
}
