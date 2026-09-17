package com.airline.seat.exception;

public class InvalidSeatOperationException extends RuntimeException {
    public InvalidSeatOperationException(String message) {
        super(message);
    }
}
