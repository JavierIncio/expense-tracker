package com.exptrack.expense.exceptions;

import java.util.UUID;

public class BudgetNotFoundException extends RuntimeException {
    public BudgetNotFoundException(UUID id, UUID userId) {
        super(String.format("Budget with ID %s for user %s not found", id, userId));
    }
}
