package com.bank.notification_service.repository;

import com.bank.notification_service.model.Notification;
import com.bank.notification_service.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository
extends JpaRepository<Notification, UUID> {
    List<Notification> findByCustomerId(UUID customerId);

    List<Notification> findByCustomerIdAndStatus(
            UUID customerId,
            NotificationStatus status
    );
}
