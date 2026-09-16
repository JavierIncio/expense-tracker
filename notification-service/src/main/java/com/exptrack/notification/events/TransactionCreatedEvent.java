package com.exptrack.notification.events;

import com.exptrack.notification.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID eventId,
        UUID transactionId,
        UUID userId,
        TransactionType type,
        BigDecimal amount,
        UUID categoryId,
        Instant occurredAt
) {}
