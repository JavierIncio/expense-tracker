package com.exptrack.identity.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String identifier) {
        super(String.format("User %s not found", identifier));
    }
}
