package com.exptrack.expense.exceptions;

public class BudgetAlreadyExistsException extends RuntimeException {
    public BudgetAlreadyExistsException() {
        super("Budget already exists for the given category, year, and month.");
    }
}
