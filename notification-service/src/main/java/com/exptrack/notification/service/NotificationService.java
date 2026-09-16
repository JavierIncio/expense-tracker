package com.exptrack.notification.service;

import com.exptrack.notification.domain.Notification;
import com.exptrack.notification.dto.NotificationResponse;
import com.exptrack.notification.events.BudgetExceededEvent;
import com.exptrack.notification.events.TransactionCreatedEvent;
import com.exptrack.notification.events.TransactionDeletedEvent;
import com.exptrack.notification.exception.NotificationNotFoundException;
import com.exptrack.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepo;

    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    public Page<NotificationResponse> list(UUID userID, Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }
         return notificationRepo.findAllByUserId(userID, pageable).map(this::toDto);
    }

    @Transactional
    public void markAsRead(UUID userID, UUID notificationId) {
        Notification notification = notificationRepo.findByIdAndUserId(notificationId, userID)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notification.setRead(true);
    }

    public void handleTransactionCreated(TransactionCreatedEvent event) {
        String message = String.format("Transaction with ID %s, for the amount %s created at %s",
                event.transactionId(), event.amount(), event.occurredAt());
        Notification notification = new Notification(event.userId(), message);
        notification.setRead(false);
        notificationRepo.save(notification);
    }

    public void handleTransactionDeleted(TransactionDeletedEvent event) {
        String message = String.format("Transaction with ID %s deleted at %s",
                event.transactionId(), event.occurredAt());
        Notification notification = new Notification(event.userId(), message);
        notification.setRead(false);
        notificationRepo.save(notification);
    }

    public void handleBudgetExceeded(BudgetExceededEvent event) {
        String message = String.format("Budget for '%s %s', set to %s, exceeded with amount %s",
                event.month(), event.year(), event.budgetAmount(), event.actualAmount());
        Notification notification = new Notification(event.userId(), message);
        notification.setRead(false);
        notificationRepo.save(notification);
    }

    private NotificationResponse toDto(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getRead(),
                notification.getCreatedAt()
        );
    }


}
