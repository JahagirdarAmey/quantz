package com.quantz.instruments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an instrument is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class InstrumentNotFoundException extends RuntimeException {
    
    public InstrumentNotFoundException(String message) {
        super(message);
    }
    
    public InstrumentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}