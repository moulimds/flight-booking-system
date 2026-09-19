package com.flight.payment.messaging;

import com.flight.payment.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishPaymentSuccess(PaymentSuccessfulEvent event) {
        logger.info("Publishing PaymentSuccessfulEvent for payment: {}", event.getPaymentReference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.PAYMENT_SUCCESS_ROUTING_KEY, event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        logger.info("Publishing PaymentFailedEvent for payment: {}", event.getPaymentReference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY, event);
    }

    public void publishPaymentRefunded(PaymentRefundedEvent event) {
        logger.info("Publishing PaymentRefundedEvent for payment: {}", event.getPaymentReference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.PAYMENT_REFUNDED_ROUTING_KEY, event);
    }
}
