package com.flighttracking.exception;

public class InvalidFlightException extends RuntimeException {

    public InvalidFlightException(String message) {
        super(message);
    }
}
