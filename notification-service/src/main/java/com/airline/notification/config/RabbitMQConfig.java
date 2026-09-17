package com.airline.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "flight.booking.exchange";

    public static final String BOOKING_QUEUE = "booking.events.queue";
    public static final String PAYMENT_QUEUE = "payment.events.queue";
    public static final String FLIGHT_QUEUE = "flight.events.queue";
    public static final String AUTH_QUEUE = "auth.events.queue";

    public static final String BOOKING_ROUTING_KEY = "booking.#";
    public static final String PAYMENT_ROUTING_KEY = "payment.#";
    public static final String FLIGHT_ROUTING_KEY = "flight.#";
    public static final String AUTH_ROUTING_KEY = "auth.#";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue bookingQueue() {
        return new Queue(BOOKING_QUEUE, true);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }

    @Bean
    public Queue flightQueue() {
        return new Queue(FLIGHT_QUEUE, true);
    }

    @Bean
    public Queue authQueue() {
        return new Queue(AUTH_QUEUE, true);
    }

    @Bean
    public Binding bookingBinding(Queue bookingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(bookingQueue).to(exchange).with(BOOKING_ROUTING_KEY);
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, TopicExchange exchange) {
        return BindingBuilder.bind(paymentQueue).to(exchange).with(PAYMENT_ROUTING_KEY);
    }

    @Bean
    public Binding flightBinding(Queue flightQueue, TopicExchange exchange) {
        return BindingBuilder.bind(flightQueue).to(exchange).with(FLIGHT_ROUTING_KEY);
    }

    @Bean
    public Binding authBinding(Queue authQueue, TopicExchange exchange) {
        return BindingBuilder.bind(authQueue).to(exchange).with(AUTH_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAutoStartup(true);
        factory.setDefaultRequeueRejected(false);
        factory.setRecoveryInterval(30000L);
        factory.setMissingQueuesFatal(false);
        return factory;
    }
}
