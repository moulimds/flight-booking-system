package com.example.booking.messaging;

import com.example.booking.dto.message.FareValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FareResponseListener {

    private final FareMessagePublisher fareMessagePublisher;

    @RabbitListener(queues = "${flight.rabbitmq.queue.fare-response:booking.fare.response.queue}")
    public void handleFareResponse(@Payload FareValidationResponse response,
                                  @Header(name = "amqp_correlationId", required = false) String correlationId) {
        String effectiveRequestId = (response.getRequestId() != null) ? response.getRequestId() : correlationId;
        log.info("Received FareValidationResponse via RabbitMQ: requestId={}, correlationId={}, valid={}, totalAmount={}",
                response.getRequestId(), correlationId, response.getValid(), response.getTotalPrice());

        if (effectiveRequestId != null) {
            fareMessagePublisher.completeRequest(effectiveRequestId, response);
        } else {
            log.error("Received FareValidationResponse without requestId or correlationId");
        }
    }
}
