package com.flight.invoice.messaging;

import com.flight.invoice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishInvoiceGenerated(InvoiceGeneratedEvent event) {
        logger.info("Publishing InvoiceGeneratedEvent for invoice: {}", event.getInvoiceNumber());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.INVOICE_GENERATED_ROUTING_KEY, event);
    }
}
