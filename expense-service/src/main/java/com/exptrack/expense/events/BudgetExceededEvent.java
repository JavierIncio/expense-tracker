package com.exptrack.expense.events;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetExceededEvent(
        UUID userId, UUID categoryId, int year, int month,
        BigDecimal budgetAmount, BigDecimal actualAmount
) {}
