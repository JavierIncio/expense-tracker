package com.exptrack.identity.web;

import com.exptrack.identity.dto.ErrorResponse;
import com.exptrack.identity.exceptions.EmailAlreadyExistsException;
import com.exptrack.identity.exceptions.InvalidRefreshTokenException;
import com.exptrack.identity.exceptions.UserNotFoundException;
import com.exptrack.identity.exceptions.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles EmailAlreadyExistsException and returns a structured error response.
     *
     * @param ex the EmailAlreadyExistsException
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        Instant.now(), 409, "Conflict",
                        ex.getMessage(), "/api/auth/register"
                ));
    }

    /**
     * Handles UsernameAlreadyExistsException and returns a structured error response.
     *
     * @param ex the UsernameAlreadyExistsException
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        Instant.now(), 409, "Conflict",
                        ex.getMessage(), "/api/auth/register"
                ));
    }

    /**
     * Handles UserNotFoundException and returns a structured error response.
     *
     * @param ex the UserNotFoundException
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        Instant.now(), 404, "Not Found",
                        ex.getMessage(), "/api/auth/login"
                ));
    }

    /**
     * Handles InvalidRefreshTokenException and returns a structured error response.
     *
     * @param ex the InvalidRefreshTokenException
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(InvalidRefreshTokenException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        Instant.now(), 401, "Unauthorized",
                        ex.getMessage(), "/api/refresh"
                ));
    }

    /**
     * Handles MethodArgumentNotValidException and returns a structured error response.
     *
     * @param ex the MethodArgumentNotValidException
     * @return ResponseEntity containing the error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        Instant.now(), 400, "Bad Request",
                        errorMessage, "/api/auth"
                ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        Instant.now(), 401, "Unauthorized",
                        ex.getMessage(), "/api/auth/login"
                ));
    }
}