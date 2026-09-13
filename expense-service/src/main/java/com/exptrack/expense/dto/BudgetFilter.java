package com.exptrack.expense.dto;

import java.util.UUID;

public record BudgetFilter(
        UUID categoryId,
        Integer year,
        Integer month
) {}
