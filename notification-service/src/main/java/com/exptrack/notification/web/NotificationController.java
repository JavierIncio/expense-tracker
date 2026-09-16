package com.exptrack.notification.web;

import com.exptrack.notification.dto.NotificationResponse;
import com.exptrack.notification.dto.UnreadCountResponse;
import com.exptrack.notification.security.UserPrincipal;
import com.exptrack.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(@AuthenticationPrincipal UserPrincipal user,
                                                                       Pageable pageable) {
        return ResponseEntity.ok(notificationService.list(user.userId(), pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(new UnreadCountResponse(notificationService.unreadCount(user.userId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal UserPrincipal user,
                                           @PathVariable UUID id) {
        notificationService.markAsRead(user.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
