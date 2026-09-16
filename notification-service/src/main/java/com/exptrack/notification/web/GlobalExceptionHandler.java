package com.exptrack.notification.web;

import com.exptrack.notification.dto.ErrorResponse;
import com.exptrack.notification.exception.NotificationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles NotificationNotFoundException and returns a structured error response.
     *
     * @param ex the NotificationNotFoundException
     * @return ResponseEntity containing the ErrorResponse and HTTP status
     */
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFoundException(NotificationNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                "/api/notifications"
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


}