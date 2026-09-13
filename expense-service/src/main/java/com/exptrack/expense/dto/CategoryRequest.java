package com.exptrack.expense.dto;

import com.exptrack.expense.domain.TransactionType;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(
        @NotNull String name,
        @NotNull TransactionType type
) {}
