package com.exptrack.notification.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(UUID id) {
        super(String.format("Notification with ID %s not found", id));
    }
}
