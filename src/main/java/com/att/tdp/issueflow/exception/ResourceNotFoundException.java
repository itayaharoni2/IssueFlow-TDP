package com.att.tdp.issueflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
/**
 * Role: Custom exception used to indicate that a requested entity could not be located in the database.
 * It maps to HTTP 404 Not Found to signal missing resources to the client.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructs a new ResourceNotFoundException with the specified error detail message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
