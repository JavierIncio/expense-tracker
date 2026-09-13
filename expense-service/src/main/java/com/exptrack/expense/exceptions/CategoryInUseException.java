package com.exptrack.expense.exceptions;

import java.util.UUID;

public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(UUID categoryId) {
        super(String.format("Category with ID '%s' is in use and cannot be deleted", categoryId));
    }
}
