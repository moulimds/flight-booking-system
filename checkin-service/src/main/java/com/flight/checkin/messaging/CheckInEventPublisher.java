package com.flight.checkin.messaging;

import com.flight.checkin.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CheckInEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(CheckInEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishCheckInCompleted(CheckInCompletedEvent event) {
        logger.info("Publishing CheckInCompletedEvent for booking reference: {}", event.getCheckInReference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.CHECKIN_COMPLETED_ROUTING_KEY, event);
    }
}
