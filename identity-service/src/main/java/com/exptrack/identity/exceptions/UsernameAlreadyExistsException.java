package com.exptrack.identity.exceptions;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super(String.format("Username '%s' is already taken", username));
    }
}
