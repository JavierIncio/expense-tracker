package com.exptrack.expense.events;

import com.exptrack.expense.config.RabbitMQConfig;
import com.exptrack.expense.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQDomainEventPublisherTest {

    @Mock RabbitTemplate rabbitTemplate;

    RabbitMQDomainEventPublisher publisher;

    final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        publisher = new RabbitMQDomainEventPublisher(rabbitTemplate);
    }

    @Test
    void publishBudgetExceeded_sendsToExchange() {
        BudgetExceededEvent event = new BudgetExceededEvent(
                userId, UUID.randomUUID(), 2026, 9,
                new BigDecimal("100.00"), new BigDecimal("150.00"));

        publisher.publishBudgetExceeded(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.BUDGET_EXCEEDED_RK, event);
    }

    @Test
    void publishTransactionCreated_sendsToExchange() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(), UUID.randomUUID(), userId, TransactionType.EXPENSE,
                new BigDecimal("50.00"), UUID.randomUUID(), Instant.now());

        publisher.publishTransactionCreated(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.TRANSACTION_CREATED_RK, event);
    }

    @Test
    void publishTransactionDeleted_sendsToExchange() {
        TransactionDeletedEvent event = new TransactionDeletedEvent(
                UUID.randomUUID(), UUID.randomUUID(), userId, Instant.now());

        publisher.publishTransactionDeleted(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.TRANSACTION_DELETED_RK, event);
    }
}