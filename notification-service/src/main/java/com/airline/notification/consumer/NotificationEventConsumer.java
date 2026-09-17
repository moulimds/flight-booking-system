package com.airline.notification.consumer;

import com.airline.notification.config.RabbitMQConfig;
import com.airline.notification.dto.AuthVerificationDTO;
import com.airline.notification.dto.BookingEventDTO;
import com.airline.notification.dto.FlightAlertEventDTO;
import com.airline.notification.dto.PaymentEventDTO;
import com.airline.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;

    public NotificationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.BOOKING_QUEUE)
    public void handleBookingEvent(BookingEventDTO event) {
        log.info("Received RabbitMQ message on [{}] for booking: {}",
                RabbitMQConfig.BOOKING_QUEUE, event.getBookingReference());
        try {
            notificationService.processBookingEvent(event);
        } catch (Exception ex) {
            log.error("Failed to process booking event: {}", ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handlePaymentEvent(PaymentEventDTO event) {
        log.info("Received RabbitMQ message on [{}] for payment: {}",
                RabbitMQConfig.PAYMENT_QUEUE, event.getPaymentId());
        try {
            notificationService.processPaymentEvent(event);
        } catch (Exception ex) {
            log.error("Failed to process payment event: {}", ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.FLIGHT_QUEUE)
    public void handleFlightAlertEvent(FlightAlertEventDTO event) {
        log.info("Received RabbitMQ message on [{}] for flight: {}",
                RabbitMQConfig.FLIGHT_QUEUE, event.getFlightNumber());
        try {
            notificationService.processFlightAlertEvent(event);
        } catch (Exception ex) {
            log.error("Failed to process flight alert event: {}", ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.AUTH_QUEUE)
    public void handleAuthVerificationEvent(AuthVerificationDTO event) {
        log.info("Received RabbitMQ message on [{}] for user: {}",
                RabbitMQConfig.AUTH_QUEUE, event.getUserId());
        try {
            notificationService.processAuthVerificationEvent(event);
        } catch (Exception ex) {
            log.error("Failed to process auth verification event: {}", ex.getMessage(), ex);
        }
    }
}
