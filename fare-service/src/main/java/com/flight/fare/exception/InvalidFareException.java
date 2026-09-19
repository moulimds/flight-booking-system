package com.flight.fare.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidFareException extends RuntimeException {
    public InvalidFareException(String message) {
        super(message);
    }
}
