package com.example.payment.dto;



import java.math.BigDecimal;

import java.time.LocalDateTime;

import com.example.payment.enums.PaymentMethod;
import com.example.payment.enums.PaymentStatus;

public record PaymentResponse(

        String paymentId,
        String bookingId,
        String customerId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        String transactionId,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}