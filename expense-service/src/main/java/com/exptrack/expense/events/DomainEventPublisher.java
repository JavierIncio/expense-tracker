package com.exptrack.expense.events;

public interface DomainEventPublisher {
    void publishBudgetExceeded(BudgetExceededEvent event);
}
