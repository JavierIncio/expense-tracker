package com.exptrack.expense.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID categoryId,
        Integer year,
        Integer month,
        BigDecimal amount
) {}
