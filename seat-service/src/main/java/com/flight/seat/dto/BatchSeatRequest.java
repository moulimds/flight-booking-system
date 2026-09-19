package com.flight.seat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class BatchSeatRequest {

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    private Integer rows;

    private java.util.List<String> seatNumbers;

    private String seatClass;

    public BatchSeatRequest() {
    }

    public BatchSeatRequest(Long flightId, Integer rows) {
        this.flightId = flightId;
        this.rows = rows;
    }

    public BatchSeatRequest(Long flightId, java.util.List<String> seatNumbers, String seatClass) {
        this.flightId = flightId;
        this.seatNumbers = seatNumbers;
        this.seatClass = seatClass;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public java.util.List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(java.util.List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public String getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(String seatClass) {
        this.seatClass = seatClass;
    }
}
