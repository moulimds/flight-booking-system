package com.example.invoice.dto;

import com.example.invoice.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceResponse(

        String invoiceId,

        String invoiceNumber,

        String bookingId,

        String paymentId,

        String customerId,

        String customerName,

        String customerEmail,

        String flightNumber,

        String source,

        String destination,

        LocalDate travelDate,

        BigDecimal baseFare,

        BigDecimal taxAmount,

        BigDecimal serviceFee,

        BigDecimal discount,

        BigDecimal totalAmount,

        String currency,

        String transactionId,

        String paymentMethod,

        String paymentStatus,

        InvoiceStatus invoiceStatus,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}