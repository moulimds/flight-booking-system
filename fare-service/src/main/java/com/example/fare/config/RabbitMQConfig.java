package com.example.fare.config;

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

    @Value("${flight.rabbitmq.queue.fare-validate:fare.validation.queue}")
    private String fareValidateQueue;

    @Value("${flight.rabbitmq.routing-key.fare-validate:fare.validate}")
    private String fareValidateRoutingKey;

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
    public Queue fareValidationQueue() {
        return QueueBuilder.durable(fareValidateQueue)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", dlqQueue)
                .build();
    }

    @Bean
    public Binding fareValidationBinding() {
        return BindingBuilder.bind(fareValidationQueue())
                .to(flightBookingExchange())
                .with(fareValidateRoutingKey);
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
