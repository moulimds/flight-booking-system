package com.flight.checkin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BaggageUpdateRequest {

    @Min(value = 0, message = "Baggage count cannot be negative")
    private Integer baggageCount;

    @DecimalMin(value = "0.0", message = "Baggage weight cannot be negative")
    private Double baggageWeight;

    public BaggageUpdateRequest() {
    }

    public BaggageUpdateRequest(Integer baggageCount, Double baggageWeight) {
        this.baggageCount = baggageCount;
        this.baggageWeight = baggageWeight;
    }

    public Integer getBaggageCount() {
        return baggageCount;
    }

    public void setBaggageCount(Integer baggageCount) {
        this.baggageCount = baggageCount;
    }

    public Double getBaggageWeight() {
        return baggageWeight;
    }

    public void setBaggageWeight(Double baggageWeight) {
        this.baggageWeight = baggageWeight;
    }
}
