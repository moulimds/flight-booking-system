package com.flight.invoice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "flight.exchange";
    public static final String INVOICE_QUEUE = "invoice.events.queue";
    public static final String INVOICE_GENERATED_ROUTING_KEY = "invoice.generated";

    @Bean
    public TopicExchange flightExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue invoiceQueue() {
        return new Queue(INVOICE_QUEUE, true);
    }

    @Bean
    public Binding invoicePaymentBinding(Queue invoiceQueue, TopicExchange flightExchange) {
        return BindingBuilder.bind(invoiceQueue).to(flightExchange).with("payment.success");
    }

    @Bean
    public Binding invoiceCancellationBinding(Queue invoiceQueue, TopicExchange flightExchange) {
        return BindingBuilder.bind(invoiceQueue).to(flightExchange).with("booking.cancelled");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
