package com.exptrack.expense.dto;

import com.exptrack.expense.domain.TransactionType;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        TransactionType type,
        Instant createdAt,
        Instant updatedAt
) {}
