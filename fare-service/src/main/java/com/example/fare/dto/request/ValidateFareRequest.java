package com.example.fare.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateFareRequest {

    @NotNull(message = "Flight ID is required")
    @Positive(message = "Flight ID must be positive")
    private Long flightId;

    @NotNull(message = "Fare ID is required")
    @Positive(message = "Fare ID must be positive")
    private Long fareId;

    @NotNull(message = "Required seats is required")
    @Min(value = 1, message = "At least 1 seat must be requested")
    private Integer requiredSeats;
}
