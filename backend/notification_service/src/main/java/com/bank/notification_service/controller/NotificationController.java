package com.bank.notification_service.controller;

import com.bank.notification_service.dto.NotificationResponse;
import com.bank.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public List<NotificationResponse> getMyNotifications(
            @RequestHeader("X-User-Id") UUID customerId,
            @RequestParam(defaultValue = "false") boolean unreadOnly)
    {
    return notificationService.getMyNotifications(customerId, unreadOnly);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable UUID notificationId,
            @RequestHeader("X-User-Id") UUID customerId){
        return notificationService.markAsRead(notificationId, customerId);
    }
}
