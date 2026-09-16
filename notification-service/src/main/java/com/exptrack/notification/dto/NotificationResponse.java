package com.exptrack.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String message,
        boolean read,
        Instant createdAt
) {}