package com.flight.booking.messaging;

import com.flight.booking.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishBookingCreated(BookingCreatedEvent event) {
        logger.info("Publishing BookingCreatedEvent for reference: {}", event.getBookingReference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.BOOKING_CREATED_ROUTING_KEY, event);
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        logger.info("Publishing BookingConfirmedEvent for reference: {}", event.getBookingReference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.BOOKING_CONFIRMED_ROUTING_KEY, event);
    }

    public void publishBookingCancelled(BookingCancelledEvent event) {
        logger.info("Publishing BookingCancelledEvent for reference: {}", event.getBookingReference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.BOOKING_CANCELLED_ROUTING_KEY, event);
    }

    public void publishBookingFailed(BookingFailedEvent event) {
        logger.info("Publishing BookingFailedEvent for reference: {}", event.getBookingReference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.BOOKING_FAILED_ROUTING_KEY, event);
    }

    public void publishBookingRescheduled(BookingRescheduledEvent event) {
        logger.info("Publishing BookingRescheduledEvent for reference: {}", event.getBookingReference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.BOOKING_RESCHEDULED_ROUTING_KEY, event);
    }
}
