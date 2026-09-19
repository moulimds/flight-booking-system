package com.flight.auth.messaging;

import com.flight.auth.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AuthEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        logger.info("Publishing UserRegisteredEvent for user: {}", event.getFullName());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.AUTH_ROUTING_KEY, event);
    }
}
