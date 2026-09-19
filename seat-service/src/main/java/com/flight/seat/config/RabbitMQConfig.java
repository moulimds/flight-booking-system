package com.flight.seat.config;

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
    public static final String SEAT_QUEUE = "seat.events.queue";

    public static final String SEAT_HELD_ROUTING_KEY = "seat.held";
    public static final String SEAT_BOOKED_ROUTING_KEY = "seat.booked";
    public static final String SEAT_RELEASED_ROUTING_KEY = "seat.released";

    @Bean
    public TopicExchange flightExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue seatQueue() {
        return new Queue(SEAT_QUEUE, true);
    }

    @Bean
    public Binding seatBinding(Queue seatQueue, TopicExchange flightExchange) {
        return BindingBuilder.bind(seatQueue).to(flightExchange).with("booking.cancelled");
    }

    @Bean
    public Binding seatCheckinBinding(Queue seatQueue, TopicExchange flightExchange) {
        return BindingBuilder.bind(seatQueue).to(flightExchange).with("checkin.completed");
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
