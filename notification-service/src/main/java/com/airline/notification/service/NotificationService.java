package com.airline.notification.service;

import com.airline.notification.dto.*;

import java.util.List;

public interface NotificationService {

    NotificationResponse sendDirectNotification(DirectNotificationRequest request);

    NotificationResponse getLogById(Long id);

    List<NotificationResponse> getLogsByUserId(String userId);

    void processBookingEvent(BookingEventDTO event);

    void processPaymentEvent(PaymentEventDTO event);

    void processFlightAlertEvent(FlightAlertEventDTO event);

    void processAuthVerificationEvent(AuthVerificationDTO event);
}
