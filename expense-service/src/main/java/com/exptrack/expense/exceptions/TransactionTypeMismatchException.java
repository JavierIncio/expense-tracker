package com.exptrack.expense.exceptions;

public class TransactionTypeMismatchException extends RuntimeException {
    public TransactionTypeMismatchException() {
        super("Transaction type does not match category type");
    }
}
