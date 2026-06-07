package com.att.tdp.issueflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
/**
 * Role: Custom exception used to indicate a state conflict within the application.
 * It maps to HTTP 409 Conflict and is typically thrown for duplicate entries or illegal state transitions.
 */
public class ConflictException extends RuntimeException {
    /**
     * Constructs a new ConflictException with the specified error detail message.
     */
    public ConflictException(String message) {
        super(message);
    }
}
