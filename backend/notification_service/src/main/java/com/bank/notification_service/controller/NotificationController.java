package com.bank.notification_service.controller;

import com.bank.notification_service.dto.NotificationResponseDTO;
import com.bank.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public List<NotificationResponseDTO> getMyNotifications(
            @RequestHeader("X-User-Id") Long customerId,
            @RequestParam(defaultValue = "false") boolean unreadOnly)
    {
    return notificationService.getMyNotifications(customerId, unreadOnly);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponseDTO markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader("X-User-Id") Long customerId){
        return notificationService.markAsRead(notificationId, customerId);
    }
}
