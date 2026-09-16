package com.exptrack.notification.service;

import com.exptrack.notification.domain.Notification;
import com.exptrack.notification.domain.TransactionType;
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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Locale LOCALE = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM", LOCALE);

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

    public long unreadCount(UUID userId) {
        return notificationRepo.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID userID, UUID notificationId) {
        Notification notification = notificationRepo.findByIdAndUserId(notificationId, userID)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notification.setRead(true);
    }

    public void handleTransactionCreated(TransactionCreatedEvent event) {
        String movement = event.type() == TransactionType.INCOME ? "ingreso" : "gasto";
        String message = String.format("Se registró un %s de %s.", movement, formatAmount(event.amount()));
        Notification notification = new Notification(event.userId(), message);
        notification.setRead(false);
        notificationRepo.save(notification);
    }

    public void handleTransactionDeleted(TransactionDeletedEvent event) {
        String message = "Se eliminó una transacción.";
        Notification notification = new Notification(event.userId(), message);
        notification.setRead(false);
        notificationRepo.save(notification);
    }

    public void handleBudgetExceeded(BudgetExceededEvent event) {
        String message = String.format("Has superado el presupuesto de %s %d: gastado %s de un límite de %s.",
                monthName(event.month()), event.year(),
                formatAmount(event.actualAmount()), formatAmount(event.budgetAmount()));
        Notification notification = new Notification(event.userId(), message);
        notification.setRead(false);
        notificationRepo.save(notification);
    }

    private String formatAmount(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(LOCALE).format(amount);
    }

    private String monthName(int month) {
        String name = MONTH_FORMATTER.format(LocalDate.of(2000, month, 1));
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
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
