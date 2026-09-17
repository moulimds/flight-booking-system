package com.airline.seat.exception;

public class OptimisticLockConflictException extends RuntimeException {
    public OptimisticLockConflictException(String message) {
        super(message);
    }
}
