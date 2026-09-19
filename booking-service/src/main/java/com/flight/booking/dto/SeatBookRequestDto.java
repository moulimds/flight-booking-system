package com.flight.booking.dto;

public class SeatBookRequestDto {
    private Long bookingId;

    public SeatBookRequestDto() {}

    public SeatBookRequestDto(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
}
