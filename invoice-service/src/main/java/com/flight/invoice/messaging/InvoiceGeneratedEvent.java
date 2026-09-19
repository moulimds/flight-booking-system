package com.flight.invoice.messaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceGeneratedEvent implements Serializable {

    private Long invoiceId;
    private String invoiceNumber;
    private Long bookingId;
    private Long paymentId;
    private Long userId;
    private BigDecimal totalAmount;
    private LocalDateTime generatedAt;

    public InvoiceGeneratedEvent() {
    }

    public InvoiceGeneratedEvent(Long invoiceId, String invoiceNumber, Long bookingId, Long paymentId,
                                 Long userId, BigDecimal totalAmount, LocalDateTime generatedAt) {
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.generatedAt = generatedAt;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
