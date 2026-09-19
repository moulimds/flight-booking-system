package com.flight.checkin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCheckInException extends RuntimeException {
    public InvalidCheckInException(String message) {
        super(message);
    }
}
