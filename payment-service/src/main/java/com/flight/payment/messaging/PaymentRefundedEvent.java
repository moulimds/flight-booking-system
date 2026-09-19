package com.flight.payment.messaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentRefundedEvent implements Serializable {

    private Long paymentId;
    private String paymentReference;
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime refundDate;

    public PaymentRefundedEvent() {
        this.refundDate = LocalDateTime.now();
    }

    public PaymentRefundedEvent(Long paymentId, String paymentReference, Long bookingId, Long userId, BigDecimal amount) {
        this.paymentId = paymentId;
        this.paymentReference = paymentReference;
        this.bookingId = bookingId;
        this.userId = userId;
        this.amount = amount;
        this.refundDate = LocalDateTime.now();
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

    public LocalDateTime getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(LocalDateTime refundDate) {
        this.refundDate = refundDate;
    }
}
