package com.exptrack.notification.events;

import java.time.Instant;
import java.util.UUID;

public record TransactionDeletedEvent(
        UUID eventId,
        UUID transactionId,
        UUID userId,
        Instant occurredAt
) {}
