package com.bank.notification_service.service;

import com.bank.notification_service.dto.NotificationResponseDTO;
import com.bank.notification_service.model.Notification;
import com.bank.notification_service.model.NotificationStatus;
import com.bank.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<NotificationResponseDTO> getMyNotifications(Long customerId, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository
                .findByCustomerIdAndStatus(customerId, NotificationStatus.UNREAD)
                : notificationRepository.findByCustomerId(customerId);

        return notifications.stream()
                .map(this::toDTO)
                .toList();
    }

    public NotificationResponseDTO markAsRead(Long notificationId, Long customerId){
        Notification notification = notificationRepository
                                    .findById(notificationId)
                                    .orElseThrow(() -> new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Notification not found"
                                    ));

        if(!notification.getCustomerId().equals(customerId)){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied"
            );
        }

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);

        return toDTO(notification);
    }

    private NotificationResponseDTO toDTO(Notification notification){
        return NotificationResponseDTO.builder()
                .notificationId(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }

}
