package com.example.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RefundRequest(

        @NotBlank(message = "Payment ID is required")
        String paymentId,

        @NotBlank(message = "Booking ID is required")
        String bookingId,

        @NotNull(message = "Refund amount is required")
        @DecimalMin(value = "1.0", message = "Refund amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Refund reason is required")
        String reason
) {
}