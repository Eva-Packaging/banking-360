package com.bank.notification_service.dto;

import com.bank.notification_service.model.NotificationStatus;
import com.bank.notification_service.model.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {

    private UUID notificationId;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
