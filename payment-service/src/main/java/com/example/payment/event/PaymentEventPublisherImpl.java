package com.example.payment.event;


import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.stereotype.Component;

import com.example.payment.entity.Payment;
import com.example.payment.entity.Refund;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentEventPublisherImpl implements PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "flight-booking-exchange";

    public PaymentEventPublisherImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishPaymentSuccess(Payment payment) {

        Map<String, Object> event = new HashMap<>();

        event.put("eventType", "PAYMENT_SUCCESS");
        event.put("paymentId", payment.getPaymentId());
        event.put("bookingId", payment.getBookingId());
        event.put("customerId", payment.getCustomerId());
        event.put("amount", payment.getAmount());
        event.put("transactionId", payment.getTransactionId());

        rabbitTemplate.convertAndSend(
                EXCHANGE,
                "payment.success",
                event
        );
    }

    @Override
    public void publishPaymentFailed(Payment payment) {

        Map<String, Object> event = new HashMap<>();

        event.put("eventType", "PAYMENT_FAILED");
        event.put("paymentId", payment.getPaymentId());
        event.put("bookingId", payment.getBookingId());
        event.put("customerId", payment.getCustomerId());
        event.put("amount", payment.getAmount());

        rabbitTemplate.convertAndSend(
                EXCHANGE,
                "payment.failed",
                event
        );
    }

    @Override
    public void publishRefundSuccess(Refund refund) {

        Map<String, Object> event = new HashMap<>();

        event.put("eventType", "REFUND_SUCCESS");
        event.put("refundId", refund.getRefundId());
        event.put("paymentId", refund.getPaymentId());
        event.put("bookingId", refund.getBookingId());
        event.put("amount", refund.getAmount());

        rabbitTemplate.convertAndSend(
                EXCHANGE,
                "refund.success",
                event
        );
    }
}