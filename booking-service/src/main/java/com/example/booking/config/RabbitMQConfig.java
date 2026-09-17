package com.example.booking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${flight.rabbitmq.exchange:flight.booking.exchange}")
    private String exchange;

    @Value("${flight.rabbitmq.queue.fare-response:booking.fare.response.queue}")
    private String fareResponseQueue;

    @Value("${flight.rabbitmq.routing-key.fare-response:fare.validation.response}")
    private String fareResponseRoutingKey;

    @Value("${flight.rabbitmq.queue.payment:booking.payment.queue}")
    private String paymentQueue;

    @Value("${flight.rabbitmq.queue.notification:booking.notification.queue}")
    private String notificationQueue;

    @Value("${flight.rabbitmq.dlx:flight.booking.dlx}")
    private String dlxExchange;

    @Value("${flight.rabbitmq.dlq:flight.booking.dlq}")
    private String dlqQueue;

    @Bean
    public TopicExchange flightBookingExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(dlxExchange, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqQueue).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(dlqQueue);
    }

    @Bean
    public Queue fareResponseQueue() {
        return QueueBuilder.durable(fareResponseQueue)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", dlqQueue)
                .build();
    }

    @Bean
    public Binding fareResponseBinding() {
        return BindingBuilder.bind(fareResponseQueue())
                .to(flightBookingExchange())
                .with(fareResponseRoutingKey);
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(paymentQueue).build();
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(flightBookingExchange())
                .with("booking.created");
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(flightBookingExchange())
                .with("booking.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
