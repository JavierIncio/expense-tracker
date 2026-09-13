package com.exptrack.expense.dto;

import com.exptrack.expense.domain.BudgetStatus;
import com.exptrack.expense.domain.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record CategorySummary(
        UUID categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal amount,
        BigDecimal budgetAmount,
        BudgetStatus budgetStatus
) {}
