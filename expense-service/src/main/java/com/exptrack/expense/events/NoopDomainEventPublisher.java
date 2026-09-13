package com.exptrack.expense.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoopDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoopDomainEventPublisher.class);

    @Override
    public void publishBudgetExceeded(BudgetExceededEvent event) {
        log.info("BudgetExceeded published (no-op): {}", event);
    }
}
