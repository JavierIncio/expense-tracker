package com.exptrack.notification.web;

import com.exptrack.notification.AbstractNotificationIntegrationTest;
import com.exptrack.notification.domain.TransactionType;
import com.exptrack.notification.events.TransactionCreatedEvent;
import com.exptrack.notification.repository.NotificationRepository;
import com.exptrack.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerIT extends AbstractNotificationIntegrationTest {

    @Autowired NotificationService notificationService;
    @Autowired NotificationRepository notificationRepo;

    @Test
    void list_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_returnsOnlyOwnNotifications() throws Exception {
        UUID otherUser = UUID.randomUUID();
        notificationService.handleTransactionCreated(createdEvent(userId, "100.00"));
        notificationService.handleTransactionCreated(createdEvent(otherUser, "999.00"));

        mockMvc.perform(get("/api/notifications").with(asUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].read").value(false))
                .andExpect(jsonPath("$.content[0].message").value(containsString("100.00")));
    }

    @Test
    void list_marksReadNotifications() throws Exception {
        notificationService.handleTransactionCreated(createdEvent(userId, "50.00"));

        mockMvc.perform(patch("/api/notifications/{id}/read", idOf(userId)).with(asUser(userId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications").with(asUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].read").value(true));
    }

    @Test
    void markAsRead_otherUsersNotification_returns404() throws Exception {
        UUID otherUser = UUID.randomUUID();
        notificationService.handleTransactionCreated(createdEvent(userId, "50.00"));

        mockMvc.perform(patch("/api/notifications/{id}/read", idOf(userId)).with(asUser(otherUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void patch_unknownId_returns404() throws Exception {
        mockMvc.perform(patch("/api/notifications/{id}/read", UUID.randomUUID()).with(asUser(userId))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private TransactionCreatedEvent createdEvent(UUID owner, String amount) {
        return new TransactionCreatedEvent(
                UUID.randomUUID(), UUID.randomUUID(), owner, TransactionType.EXPENSE,
                new BigDecimal(amount), UUID.randomUUID(), Instant.now());
    }

    private UUID idOf(UUID owner) {
        return notificationRepo.findAllByUserId(owner, PageRequest.of(0, 1))
                .getContent().get(0).getId();
    }
}