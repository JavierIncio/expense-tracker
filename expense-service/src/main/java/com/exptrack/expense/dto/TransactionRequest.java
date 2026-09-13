package com.exptrack.expense.dto;

import com.exptrack.expense.domain.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(
        @NotNull TransactionType type,
        String description,
        @NotNull @Positive BigDecimal amount,
        @NotNull UUID categoryId,
        @NotNull LocalDate date
) {}
