package com.exptrack.expense.exceptions;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(UUID id, UUID userId) {
        super(String.format("Transaction with ID %s for user %s not found", id, userId));
    }
}
