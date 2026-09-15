package com.example.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceRequest(

        @NotBlank
        String bookingId,

        String paymentId,

        @NotBlank
        String customerId,

        @NotBlank
        String customerName,

        @Email
        @NotBlank
        String customerEmail,

        @NotBlank
        String flightNumber,

        @NotBlank
        String source,

        @NotBlank
        String destination,

        @NotNull
        LocalDate travelDate,

        @NotNull
        @DecimalMin(value = "0.0")
        BigDecimal baseFare,

        @NotNull
        @DecimalMin(value = "0.0")
        BigDecimal taxAmount,

        @NotNull
        @DecimalMin(value = "0.0")
        BigDecimal serviceFee,

        @NotNull
        @DecimalMin(value = "0.0")
        BigDecimal discount,

        @NotNull
        @DecimalMin(value = "0.0")
        BigDecimal totalAmount,

        @NotBlank
        String currency,

        String transactionId,

        String paymentMethod,

        String paymentStatus

) {
}