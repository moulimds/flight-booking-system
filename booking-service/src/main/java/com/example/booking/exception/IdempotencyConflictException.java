package com.example.booking.exception;

import com.example.booking.dto.response.BookingResponse;
import lombok.Getter;

@Getter
public class IdempotencyConflictException extends RuntimeException {
    private final BookingResponse existingBooking;

    public IdempotencyConflictException(String message, BookingResponse existingBooking) {
        super(message);
        this.existingBooking = existingBooking;
    }
}
