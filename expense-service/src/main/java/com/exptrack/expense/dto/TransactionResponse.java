package com.exptrack.expense.dto;

import com.exptrack.expense.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        String description,
        BigDecimal amount,
        UUID categoryId,
        LocalDate date,
        Instant createdAt
) {}
