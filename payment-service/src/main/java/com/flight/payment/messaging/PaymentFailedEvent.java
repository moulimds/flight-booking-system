package com.flight.payment.messaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentFailedEvent implements Serializable {

    private Long paymentId;
    private String paymentReference;
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime transactionDate;

    public PaymentFailedEvent() {
        this.transactionDate = LocalDateTime.now();
    }

    public PaymentFailedEvent(Long paymentId, String paymentReference, Long bookingId, Long userId,
                              BigDecimal amount, String reason) {
        this.paymentId = paymentId;
        this.paymentReference = paymentReference;
        this.bookingId = bookingId;
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.transactionDate = LocalDateTime.now();
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}
