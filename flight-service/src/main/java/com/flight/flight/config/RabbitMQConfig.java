package com.flight.flight.config;

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
    public static final String SEARCH_QUEUE = "search.events.queue";

    public static final String FLIGHT_CREATED_ROUTING_KEY = "flight.created";
    public static final String FLIGHT_UPDATED_ROUTING_KEY = "flight.updated";
    public static final String FLIGHT_DELETED_ROUTING_KEY = "flight.deleted";
    public static final String FLIGHT_RESCHEDULED_ROUTING_KEY = "flight.rescheduled";

    @Bean
    public TopicExchange flightExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue searchQueue() {
        return new Queue(SEARCH_QUEUE, true);
    }

    @Bean
    public Binding searchBinding(Queue searchQueue, TopicExchange flightExchange) {
        return BindingBuilder.bind(searchQueue).to(flightExchange).with("flight.#");
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
