package com.bank.notification_service.dto;

import com.bank.notification_service.model.NotificationStatus;
import com.bank.notification_service.model.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponseDTO {

    private Long notificationId;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationStatus status;
    private LocalDateTime createdAt;
}
