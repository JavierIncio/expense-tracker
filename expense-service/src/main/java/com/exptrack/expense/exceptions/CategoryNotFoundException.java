package com.exptrack.expense.exceptions;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(UUID categoryId, UUID userId) {
        super(String.format("Category with ID %s for user %s not found", categoryId, userId));
    }
}
