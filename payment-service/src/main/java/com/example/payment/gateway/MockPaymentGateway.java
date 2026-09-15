package com.example.payment.gateway;

import com.example.payment.entity.Payment;

public interface MockPaymentGateway {

    String processPayment(Payment payment);

    boolean verifyPayment(String transactionId);

    String processRefund(String transactionId);
}