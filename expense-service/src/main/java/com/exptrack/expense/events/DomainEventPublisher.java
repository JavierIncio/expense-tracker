package com.exptrack.expense.events;

public interface DomainEventPublisher {
    void publishBudgetExceeded(BudgetExceededEvent event);
    void publishTransactionCreated(TransactionCreatedEvent event);
    void publishTransactionDeleted(TransactionDeletedEvent event);
}
