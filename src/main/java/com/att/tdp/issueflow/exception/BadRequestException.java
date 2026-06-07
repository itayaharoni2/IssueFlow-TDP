package com.att.tdp.issueflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
/**
 * Role: Custom exception used to indicate that the client sent an invalid request.
 * It maps to HTTP 400 Bad Request and provides specific details about the violation.
 */
public class BadRequestException extends RuntimeException {
    /**
     * Constructs a new BadRequestException with the specified error detail message.
     */
    public BadRequestException(String message) {
        super(message);
    }
}
