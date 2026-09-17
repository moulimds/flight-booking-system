package com.example.booking.messaging;

import com.example.booking.dto.message.BookingCancelledEvent;
import com.example.booking.dto.message.BookingConfirmedEvent;
import com.example.booking.dto.message.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${flight.rabbitmq.exchange:flight.booking.exchange}")
    private String exchange;

    @Value("${flight.rabbitmq.routing-key.booking-created:booking.created}")
    private String bookingCreatedRoutingKey;

    @Value("${flight.rabbitmq.routing-key.booking-confirmed:booking.confirmed}")
    private String bookingConfirmedRoutingKey;

    @Value("${flight.rabbitmq.routing-key.booking-cancelled:booking.cancelled}")
    private String bookingCancelledRoutingKey;

    public void publishBookingCreated(BookingCreatedEvent event) {
        log.info("Publishing booking.created event: bookingId={}, pnr={}, totalAmount={}",
                event.getBookingId(), event.getPnr(), event.getTotalAmount());
        publish(bookingCreatedRoutingKey, event);
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Publishing booking.confirmed event: bookingId={}, pnr={}",
                event.getBookingId(), event.getPnr());
        publish(bookingConfirmedRoutingKey, event);
    }

    public void publishBookingCancelled(BookingCancelledEvent event) {
        log.info("Publishing booking.cancelled event: bookingId={}, pnr={}, refundAmount={}",
                event.getBookingId(), event.getPnr(), event.getRefundAmount());
        publish(bookingCancelledRoutingKey, event);
    }

    private void publish(String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
        } catch (Exception e) {
            log.error("Failed to publish event with routingKey={} to exchange={}: {}", routingKey, exchange, e.getMessage());
        }
    }
}
