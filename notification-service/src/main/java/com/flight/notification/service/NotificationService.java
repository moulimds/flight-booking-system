package com.flight.notification.service;

import com.flight.notification.dto.NotificationRequest;
import com.flight.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse sendNotification(NotificationRequest request);
    List<NotificationResponse> getAllNotifications();
    NotificationResponse getNotificationById(Long id);
    List<NotificationResponse> getNotificationsByUserId(Long userId);
    List<NotificationResponse> getNotificationsByBookingId(Long bookingId);
}
