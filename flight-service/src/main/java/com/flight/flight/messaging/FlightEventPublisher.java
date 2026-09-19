package com.flight.flight.messaging;

import com.flight.flight.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlightEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(FlightEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishFlightCreated(FlightCreatedEvent event) {
        logger.info("Publishing FlightCreatedEvent for flight: {}", event.getFlightNumber());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.FLIGHT_CREATED_ROUTING_KEY, event);
    }

    public void publishFlightUpdated(FlightUpdatedEvent event) {
        logger.info("Publishing FlightUpdatedEvent for flight: {}", event.getFlightNumber());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.FLIGHT_UPDATED_ROUTING_KEY, event);
    }

    public void publishFlightDeleted(FlightDeletedEvent event) {
        logger.info("Publishing FlightDeletedEvent for flight: {}", event.getFlightNumber());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.FLIGHT_DELETED_ROUTING_KEY, event);
    }

    public void publishFlightRescheduled(FlightRescheduledEvent event) {
        logger.info("Publishing FlightRescheduledEvent for flight: {}", event.getFlightNumber());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.FLIGHT_RESCHEDULED_ROUTING_KEY, event);
    }
}
