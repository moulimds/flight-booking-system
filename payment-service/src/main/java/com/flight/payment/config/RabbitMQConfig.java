package com.flight.payment.config;

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
    public static final String PAYMENT_QUEUE = "payment.events.queue";

    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    public static final String PAYMENT_REFUNDED_ROUTING_KEY = "payment.refunded";

    @Bean
    public TopicExchange flightExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }

    @Bean
    public Binding paymentCancellationBinding(Queue paymentQueue, TopicExchange flightExchange) {
        return BindingBuilder.bind(paymentQueue).to(flightExchange).with("booking.cancelled");
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
