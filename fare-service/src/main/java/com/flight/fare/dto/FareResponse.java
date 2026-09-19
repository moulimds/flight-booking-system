package com.flight.fare.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FareResponse {

    private Long id;
    private Long flightId;
    private BigDecimal baseFare;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal finalFare;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FareResponse() {
    }

    public FareResponse(Long id, Long flightId, BigDecimal baseFare, BigDecimal tax, BigDecimal discount,
                        BigDecimal finalFare, String currency, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.flightId = flightId;
        this.baseFare = baseFare;
        this.tax = tax;
        this.discount = discount;
        this.finalFare = finalFare;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(BigDecimal finalFare) {
        this.finalFare = finalFare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
