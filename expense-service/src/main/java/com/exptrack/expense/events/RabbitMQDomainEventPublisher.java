package com.exptrack.expense.events;

import com.exptrack.expense.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQDomainEventPublisher implements DomainEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQDomainEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishBudgetExceeded(BudgetExceededEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.BUDGET_EXCEEDED_RK, event);
    }

    @Override
    public void publishTransactionCreated(TransactionCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.TRANSACTION_CREATED_RK, event);
    }

    @Override
    public void publishTransactionDeleted(TransactionDeletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.TRANSACTION_DELETED_RK, event);
    }
}