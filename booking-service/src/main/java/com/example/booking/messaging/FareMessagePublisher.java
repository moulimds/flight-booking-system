package com.example.booking.messaging;

import com.example.booking.dto.message.FareValidationRequest;
import com.example.booking.dto.message.FareValidationResponse;
import com.example.booking.exception.FareValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FareMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${flight.rabbitmq.exchange:flight.booking.exchange}")
    private String exchange;

    @Value("${flight.rabbitmq.routing-key.fare-validate:fare.validate}")
    private String fareValidateRoutingKey;

    @Value("${flight.rabbitmq.routing-key.fare-response:fare.validation.response}")
    private String fareResponseRoutingKey;

    @Value("${flight.rabbitmq.fare-validation-timeout-ms:5000}")
    private long timeoutMs;

    private final Map<String, CompletableFuture<FareValidationResponse>> pendingRequests = new ConcurrentHashMap<>();

    public FareValidationResponse requestFareValidation(Long flightId, Long fareId, int requiredSeats) {
        String requestId = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Sending FareValidationRequest via RabbitMQ: requestId={}, flightId={}, fareId={}, seats={}",
                requestId, flightId, fareId, requiredSeats);

        FareValidationRequest request = FareValidationRequest.builder()
                .requestId(requestId)
                .flightId(flightId)
                .fareId(fareId)
                .requiredSeats(requiredSeats)
                .build();

        CompletableFuture<FareValidationResponse> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        try {
            rabbitTemplate.convertAndSend(exchange, fareValidateRoutingKey, request, message -> {
                MessageProperties props = message.getMessageProperties();
                props.setCorrelationId(requestId);
                props.setReplyTo(fareResponseRoutingKey);
                return message;
            });

            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            log.error("Fare validation request {} timed out after {} ms", requestId, timeoutMs);
            throw new FareValidationException("Fare Service request timed out after " + timeoutMs + " ms. Service might be unavailable.");
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Fare validation request {} was interrupted", requestId);
            throw new FareValidationException("Fare validation was interrupted: " + ie.getMessage());
        } catch (Exception e) {
            log.error("Failed during fare validation request {}: {}", requestId, e.getMessage());
            throw new FareValidationException("Fare validation failed: " + e.getMessage());
        } finally {
            pendingRequests.remove(requestId);
        }
    }

    public void completeRequest(String requestId, FareValidationResponse response) {
        CompletableFuture<FareValidationResponse> future = pendingRequests.get(requestId);
        if (future != null) {
            log.info("Completing pending fare validation request with requestId={}", requestId);
            future.complete(response);
        } else {
            log.warn("Received response for unknown or already completed requestId={}", requestId);
        }
    }
}
