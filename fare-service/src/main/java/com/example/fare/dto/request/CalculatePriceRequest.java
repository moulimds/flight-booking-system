package com.example.fare.dto.request;

import com.example.fare.entity.CabinClass;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalculatePriceRequest {

    @NotNull(message = "Fare ID is required")
    private Long fareId;

    private Long flightId;

    @Min(value = 1, message = "Passenger count must be at least 1")
    private Integer passengerCount;

    private CabinClass cabinClass;
}
