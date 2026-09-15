package com.example.payment.dto;




import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.payment.enums.RefundStatus;

public record RefundResponse(

        String refundId,
        String paymentId,
        String bookingId,
        BigDecimal amount,
        String reason,
        String refundTransactionId,
        RefundStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}