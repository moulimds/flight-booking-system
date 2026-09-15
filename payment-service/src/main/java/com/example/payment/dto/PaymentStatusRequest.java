package com.example.payment.dto;

import com.example.payment.enums.PaymentStatus;

import jakarta.validation.constraints.NotNull;

public record PaymentStatusRequest(

        @NotNull(message = "Payment status is required")
        PaymentStatus status
) {
}