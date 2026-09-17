package com.example.invoice.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE =
            "flight-booking-exchange";

    public static final String PAYMENT_SUCCESS_QUEUE =
            "invoice-payment-success-queue";

    public static final String PAYMENT_SUCCESS_ROUTING_KEY =
            "payment.success";


    @Bean
    public DirectExchange flightBookingExchange() {

        return new DirectExchange(EXCHANGE);
    }


    @Bean
    public Queue paymentSuccessQueue() {

        return new Queue(
                PAYMENT_SUCCESS_QUEUE,
                true
        );
    }


    @Bean
    public Binding paymentSuccessBinding(
            Queue paymentSuccessQueue,
            DirectExchange flightBookingExchange) {

        return BindingBuilder
                .bind(paymentSuccessQueue)
                .to(flightBookingExchange)
                .with(PAYMENT_SUCCESS_ROUTING_KEY);
    }
}