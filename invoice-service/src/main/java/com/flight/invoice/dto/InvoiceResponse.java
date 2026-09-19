package com.flight.invoice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private Long bookingId;
    private Long paymentId;
    private Long userId;
    private String passengerName;
    private BigDecimal amount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private String invoiceStatus;
    private LocalDateTime generatedAt;

    public InvoiceResponse() {
    }

    public InvoiceResponse(Long id, String invoiceNumber, Long bookingId, Long paymentId, Long userId,
                           String passengerName, BigDecimal amount, BigDecimal tax, BigDecimal totalAmount,
                           String invoiceStatus, LocalDateTime generatedAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.userId = userId;
        this.passengerName = passengerName;
        this.amount = amount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.invoiceStatus = invoiceStatus;
        this.generatedAt = generatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
