package com.flight.booking.config;

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
    public static final String BOOKING_QUEUE = "booking.events.queue";

    public static final String BOOKING_CREATED_ROUTING_KEY = "booking.created";
    public static final String BOOKING_CONFIRMED_ROUTING_KEY = "booking.confirmed";
    public static final String BOOKING_CANCELLED_ROUTING_KEY = "booking.cancelled";
    public static final String BOOKING_FAILED_ROUTING_KEY = "booking.failed";
    public static final String BOOKING_RESCHEDULED_ROUTING_KEY = "booking.rescheduled";

    @Bean
    public TopicExchange flightExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue bookingQueue() {
        return new Queue(BOOKING_QUEUE, true);
    }

    @Bean
    public Binding bookingPaymentSuccessBinding(Queue bookingQueue, TopicExchange flightExchange) {
        return BindingBuilder.bind(bookingQueue).to(flightExchange).with("payment.success");
    }

    @Bean
    public Binding bookingPaymentFailedBinding(Queue bookingQueue, TopicExchange flightExchange) {
        return BindingBuilder.bind(bookingQueue).to(flightExchange).with("payment.failed");
    }

    @Bean
    public Binding bookingCheckinBinding(Queue bookingQueue, TopicExchange flightExchange) {
        return BindingBuilder.bind(bookingQueue).to(flightExchange).with("checkin.completed");
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
