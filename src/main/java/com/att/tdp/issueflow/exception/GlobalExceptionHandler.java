package com.att.tdp.issueflow.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
/**
 * Role: Centralized exception handling component for the entire application.
 * It intercepts various exceptions thrown by controllers and translates them into structured JSON error responses with appropriate HTTP status codes.
 */
public class GlobalExceptionHandler {

    /**
     * Handles validation errors resulting from @Valid annotations on request bodies.
     * Returns an HTTP 400 response with a list of field-specific error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        return error(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    /**
     * Handles constraint violations, typically from invalid path variables or query parameters.
     * Returns an HTTP 400 response with the violation message.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /**
     * Handles custom BadRequestException thrown for specific business rule violations.
     * Returns an HTTP 400 response detailing the business error.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /**
     * Handles standard IllegalArgumentException indicating invalid inputs.
     * Returns an HTTP 400 response with the exception message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /**
     * Handles errors parsing the incoming HTTP request, such as malformed JSON or invalid enum values.
     * Returns an HTTP 400 response with the root cause of the parsing failure.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body: " + rootCause(ex), null);
    }

    /**
     * Handles custom ResourceNotFoundException when a requested entity does not exist.
     * Returns an HTTP 404 response.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /**
     * Handles optimistic locking failures resulting from concurrent modifications.
     * Returns an HTTP 409 response advising the client to retry.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return error(HttpStatus.CONFLICT,
                "Concurrent modification conflict — please retry with fresh data", null);
    }

    /**
     * Handles custom ConflictException used for logical application state conflicts.
     * Returns an HTTP 409 response.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /**
     * Handles database-level data integrity violations like unique constraint failures.
     * Returns an HTTP 409 response exposing the root cause from the database driver.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return error(HttpStatus.CONFLICT, "Data integrity violation: " + rootCause(ex), null);
    }

    /**
     * Handles authentication failures, such as invalid credentials or missing tokens.
     * Returns an HTTP 401 response.
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(org.springframework.security.core.AuthenticationException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    /**
     * Handles authorization failures when an authenticated user lacks required roles/permissions.
     * Returns an HTTP 403 response.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    /**
     * Fallback handler for all unexpected or unhandled exceptions.
     * Returns an HTTP 500 response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage(), null);
    }

    // ─── private helpers ──────────────────────────────────────────────────

    /**
     * Helper method to construct a consistent JSON error response body.
     */
    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message, List<String> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (details != null) body.put("details", details);
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Helper method to drill down into a nested exception to extract the innermost root cause message.
     */
    private String rootCause(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage();
    }
}
