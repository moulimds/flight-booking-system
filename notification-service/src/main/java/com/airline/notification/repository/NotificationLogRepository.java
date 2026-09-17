package com.airline.notification.repository;

import com.airline.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(String userId);

    List<NotificationLog> findByRecipientOrderByCreatedAtDesc(String recipient);
}
