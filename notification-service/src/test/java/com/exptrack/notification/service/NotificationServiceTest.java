package com.exptrack.notification.service;

import com.exptrack.notification.domain.Notification;
import com.exptrack.notification.domain.TransactionType;
import com.exptrack.notification.dto.NotificationResponse;
import com.exptrack.notification.events.BudgetExceededEvent;
import com.exptrack.notification.events.TransactionCreatedEvent;
import com.exptrack.notification.events.TransactionDeletedEvent;
import com.exptrack.notification.exception.NotificationNotFoundException;
import com.exptrack.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepo;

    NotificationService service;

    final UUID userId = UUID.randomUUID();
    final UUID notificationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new NotificationService(notificationRepo);
    }

    @Test
    void list_unsorted_appliesCreatedAtDesc() {
        when(notificationRepo.findAllByUserId(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Notification(userId, "hola"))));

        service.list(userId, PageRequest.of(0, 20));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepo).findAllByUserId(eq(userId), captor.capture());
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void list_keepsGivenSort() {
        when(notificationRepo.findAllByUserId(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "message"));
        service.list(userId, requested);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepo).findAllByUserId(eq(userId), captor.capture());
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "message"));
    }

    @Test
    void list_mapsToDtos() {
        Notification notification = new Notification(userId, "transacción creada");
        when(notificationRepo.findAllByUserId(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));

        Page<NotificationResponse> page = service.list(userId, PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).message()).isEqualTo("transacción creada");
        assertThat(page.getContent().get(0).read()).isFalse();
    }

    @Test
    void markAsRead_marksOwnedNotificationAsRead() {
        Notification notification = new Notification(userId, "hola");
        when(notificationRepo.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.of(notification));

        service.markAsRead(userId, notificationId);

        assertThat(notification.getRead()).isTrue();
    }

    @Test
    void markAsRead_notFound_throws() {
        when(notificationRepo.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(userId, notificationId))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void handleTransactionCreated_persistsNotification() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(), UUID.randomUUID(), userId, TransactionType.EXPENSE,
                new BigDecimal("50.00"), UUID.randomUUID(), Instant.now());

        service.handleTransactionCreated(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getRead()).isFalse();
        assertThat(saved.getMessage()).contains("gasto")
                .contains("50,00");
    }

    @Test
    void handleTransactionCreated_income_usesIngresoWording() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(), UUID.randomUUID(), userId, TransactionType.INCOME,
                new BigDecimal("250.00"), UUID.randomUUID(), Instant.now());

        service.handleTransactionCreated(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(captor.capture());
        assertThat(captor.getValue().getMessage()).contains("ingreso")
                .contains("250,00");
    }

    @Test
    void handleTransactionDeleted_persistsNotification() {
        TransactionDeletedEvent event = new TransactionDeletedEvent(
                UUID.randomUUID(), UUID.randomUUID(), userId, Instant.now());

        service.handleTransactionDeleted(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getRead()).isFalse();
        assertThat(captor.getValue().getMessage()).isEqualTo("Se eliminó una transacción.");
    }

    @Test
    void handleBudgetExceeded_persistsNotificationWithActualAmount() {
        BudgetExceededEvent event = new BudgetExceededEvent(
                userId, UUID.randomUUID(), 2026, 9,
                new BigDecimal("100.00"), new BigDecimal("150.00"));

        service.handleBudgetExceeded(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getRead()).isFalse();
        assertThat(saved.getMessage()).contains("Septiembre")
                .contains("2026")
                .contains("150,00");
    }
}