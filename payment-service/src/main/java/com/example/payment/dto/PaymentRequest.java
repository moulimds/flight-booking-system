package com.example.payment.dto;



import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import com.example.payment.enums.PaymentMethod;

public record PaymentRequest(

        @NotBlank(message = "Booking ID is required")
        String bookingId,

        @NotBlank(message = "Customer ID is required")
        String customerId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.0", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}