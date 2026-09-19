package com.flighttracking.exception;

public class DuplicateTrackingException extends RuntimeException {

    public DuplicateTrackingException(String message) {
        super(message);
    }
}
