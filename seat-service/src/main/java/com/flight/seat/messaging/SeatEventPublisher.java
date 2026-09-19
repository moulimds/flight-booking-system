package com.flight.seat.messaging;

import com.flight.seat.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SeatEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SeatEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishSeatHeld(SeatHeldEvent event) {
        logger.info("Publishing SeatHeldEvent for seat id: {}", event.getSeatId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.SEAT_HELD_ROUTING_KEY, event);
    }

    public void publishSeatBooked(SeatBookedEvent event) {
        logger.info("Publishing SeatBookedEvent for seat id: {}", event.getSeatId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.SEAT_BOOKED_ROUTING_KEY, event);
    }

    public void publishSeatReleased(SeatReleasedEvent event) {
        logger.info("Publishing SeatReleasedEvent for seat id: {}", event.getSeatId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.SEAT_RELEASED_ROUTING_KEY, event);
    }
}
