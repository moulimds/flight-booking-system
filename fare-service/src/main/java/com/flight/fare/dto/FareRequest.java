package com.flight.fare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class FareRequest {

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotNull(message = "Base fare is required")
    @PositiveOrZero(message = "Base fare cannot be negative")
    private BigDecimal baseFare;

    @NotNull(message = "Tax is required")
    @PositiveOrZero(message = "Tax cannot be negative")
    private BigDecimal tax;

    @NotNull(message = "Discount is required")
    @PositiveOrZero(message = "Discount cannot be negative")
    private BigDecimal discount;

    private String currency;

    public FareRequest() {
    }

    public FareRequest(Long flightId, BigDecimal baseFare, BigDecimal tax, BigDecimal discount, String currency) {
        this.flightId = flightId;
        this.baseFare = baseFare;
        this.tax = tax;
        this.discount = discount;
        this.currency = currency;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
