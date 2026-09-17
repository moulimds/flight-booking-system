package com.example.payment.gateway;


import org.springframework.stereotype.Component;

import com.example.payment.entity.Payment;

import java.util.UUID;

@Component
public class MockPaymentGatewayImpl implements MockPaymentGateway {

    @Override
    public String processPayment(Payment payment) {

        /*
         * Mock behaviour:
         * Amount ending with .99 -> FAILED
         * Everything else -> SUCCESS
         *
         * This makes it easy to demonstrate both scenarios.
         */

        if (payment.getAmount().remainder(java.math.BigDecimal.ONE)
                .compareTo(new java.math.BigDecimal("0.99")) == 0) {

            return "FAILED";
        }

        return "TXN-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    @Override
    public boolean verifyPayment(String transactionId) {

        return transactionId != null &&
                transactionId.startsWith("TXN-");
    }

    @Override
    public String processRefund(String transactionId) {

        return "REF-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}