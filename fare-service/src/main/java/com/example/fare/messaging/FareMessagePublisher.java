package com.example.fare.messaging;

import com.example.fare.dto.response.FareResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FareMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${flight.rabbitmq.exchange:flight.booking.exchange}")
    private String exchange;

    @Value("${flight.rabbitmq.routing-key.fare-created:fare.created}")
    private String fareCreatedRoutingKey;

    @Value("${flight.rabbitmq.routing-key.fare-updated:fare.updated}")
    private String fareUpdatedRoutingKey;

    @Value("${flight.rabbitmq.routing-key.fare-status-changed:fare.status.changed}")
    private String fareStatusChangedRoutingKey;

    public void publishFareCreated(FareResponse fare) {
        log.info("Publishing fare.created event for fareId={}, flightId={}", fare.getId(), fare.getFlightId());
        publishEvent(fareCreatedRoutingKey, fare);
    }

    public void publishFareUpdated(FareResponse fare) {
        log.info("Publishing fare.updated event for fareId={}, flightId={}", fare.getId(), fare.getFlightId());
        publishEvent(fareUpdatedRoutingKey, fare);
    }

    public void publishFareStatusChanged(Long fareId, String status) {
        log.info("Publishing fare.status.changed event for fareId={}, status={}", fareId, status);
        Map<String, Object> event = new HashMap<>();
        event.put("fareId", fareId);
        event.put("status", status);
        publishEvent(fareStatusChangedRoutingKey, event);
    }

    private void publishEvent(String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
        } catch (Exception e) {
            log.error("Failed to publish event to exchange={} with routingKey={}: {}", exchange, routingKey, e.getMessage());
        }
    }
}
