package com.exptrack.notification.events;

import com.exptrack.notification.config.RabbitMQConfig;
import com.exptrack.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_CREATED_QUEUE)
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {
        notificationService.handleTransactionCreated(event);
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_DELETED_QUEUE)
    public void handleTransactionDeletedEvent(TransactionDeletedEvent event) {
        notificationService.handleTransactionDeleted(event);
    }

    @RabbitListener(queues = RabbitMQConfig.BUDGET_EXCEEDED_QUEUE)
    public void handleBudgetExceededEvent(BudgetExceededEvent event) {
        notificationService.handleBudgetExceeded(event);
    }
}