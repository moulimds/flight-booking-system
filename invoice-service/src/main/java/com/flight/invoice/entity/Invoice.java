package com.flight.invoice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "passenger_name", length = 150)
    private String passengerName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "invoice_status", nullable = false, length = 50)
    private String invoiceStatus = "GENERATED"; // GENERATED, CANCELLED

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    public Invoice() {
    }

    public Invoice(Long id, String invoiceNumber, Long bookingId, Long paymentId, Long userId,
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

    @PrePersist
    protected void onCreate() {
        this.generatedAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", bookingId=" + bookingId +
                ", paymentId=" + paymentId +
                ", userId=" + userId +
                ", passengerName='" + passengerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", invoiceStatus='" + invoiceStatus + '\'' +
                '}';
    }
}
