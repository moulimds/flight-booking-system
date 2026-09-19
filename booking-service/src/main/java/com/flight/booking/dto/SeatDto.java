package com.flight.booking.dto;

public class SeatDto {
    private Long id;
    private Long flightId;
    private String seatNumber;
    private String seatClass;
    private String status;
    private Long bookingId;

    public SeatDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getSeatClass() { return seatClass; }
    public void setSeatClass(String seatClass) { this.seatClass = seatClass; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
}
