package com.flight.invoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class InvoiceRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private Long paymentId;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String passengerName;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private BigDecimal tax;
    private BigDecimal totalAmount;

    public InvoiceRequest() {
    }

    public InvoiceRequest(Long bookingId, Long paymentId, Long userId, String passengerName,
                          BigDecimal amount, BigDecimal tax, BigDecimal totalAmount) {
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.userId = userId;
        this.passengerName = passengerName;
        this.amount = amount;
        this.tax = tax;
        this.totalAmount = totalAmount;
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
}
