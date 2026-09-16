package com.exptrack.notification.events;

import com.exptrack.notification.domain.TransactionType;
import com.exptrack.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock NotificationService notificationService;

    NotificationEventListener listener;

    final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        listener = new NotificationEventListener(notificationService);
    }

    @Test
    void handleTransactionCreatedEvent_delegatesToService() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(), UUID.randomUUID(), userId, TransactionType.INCOME,
                new BigDecimal("100.00"), UUID.randomUUID(), Instant.now());

        listener.handleTransactionCreatedEvent(event);

        verify(notificationService).handleTransactionCreated(event);
    }

    @Test
    void handleTransactionDeletedEvent_delegatesToService() {
        TransactionDeletedEvent event = new TransactionDeletedEvent(
                UUID.randomUUID(), UUID.randomUUID(), userId, Instant.now());

        listener.handleTransactionDeletedEvent(event);

        verify(notificationService).handleTransactionDeleted(event);
    }

    @Test
    void handleBudgetExceededEvent_delegatesToService() {
        BudgetExceededEvent event = new BudgetExceededEvent(
                userId, UUID.randomUUID(), 2026, 9,
                new BigDecimal("100.00"), new BigDecimal("150.00"));

        listener.handleBudgetExceededEvent(event);

        verify(notificationService).handleBudgetExceeded(event);
    }
}