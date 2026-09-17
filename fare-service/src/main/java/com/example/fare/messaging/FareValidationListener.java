package com.example.fare.messaging;

import com.example.fare.dto.message.FareValidationRequest;
import com.example.fare.dto.message.FareValidationResponse;
import com.example.fare.service.FareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FareValidationListener {

    private final FareService fareService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${flight.rabbitmq.exchange:flight.booking.exchange}")
    private String exchange;

    @Value("${flight.rabbitmq.routing-key.fare-response:fare.validation.response}")
    private String fareResponseRoutingKey;

    @RabbitListener(queues = "${flight.rabbitmq.queue.fare-validate:fare.validation.queue}")
    public void handleFareValidationRequest(@Payload FareValidationRequest request,
                                           @Header(name = "amqp_correlationId", required = false) String correlationId,
                                           @Header(name = "amqp_replyTo", required = false) String replyTo) {

        String effectiveRequestId = (request.getRequestId() != null) ? request.getRequestId() : correlationId;
        log.info("Received FareValidationRequest via RabbitMQ: requestId={}, correlationId={}, flightId={}, fareId={}, seats={}",
                request.getRequestId(), correlationId, request.getFlightId(), request.getFareId(), request.getRequiredSeats());

        FareValidationResponse response;
        try {
            response = fareService.validateFare(
                    request.getFlightId(),
                    request.getFareId(),
                    request.getRequiredSeats(),
                    effectiveRequestId
            );
        } catch (Exception e) {
            log.error("Error occurred while validating fare: {}", e.getMessage(), e);
            response = FareValidationResponse.builder()
                    .requestId(effectiveRequestId)
                    .valid(false)
                    .flightId(request.getFlightId())
                    .fareId(request.getFareId())
                    .message("Validation error: " + e.getMessage())
                    .build();
        }

        String targetRoutingKey = (replyTo != null && !replyTo.isBlank()) ? replyTo : fareResponseRoutingKey;
        log.info("Sending FareValidationResponse to exchange={}, routingKey={}, requestId={}, valid={}",
                exchange, targetRoutingKey, response.getRequestId(), response.getValid());

        rabbitTemplate.convertAndSend(exchange, targetRoutingKey, response, message -> {
            MessageProperties props = message.getMessageProperties();
            if (effectiveRequestId != null) {
                props.setCorrelationId(effectiveRequestId);
            }
            return message;
        });
    }
}
