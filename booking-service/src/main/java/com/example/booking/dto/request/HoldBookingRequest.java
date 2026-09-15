package com.example.booking.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldBookingRequest {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Flight ID is required")
    @Positive(message = "Flight ID must be positive")
    private Long flightId;

    @NotNull(message = "Fare ID is required")
    @Positive(message = "Fare ID must be positive")
    private Long fareId;

    @NotEmpty(message = "At least one passenger is required")
    @Valid
    private List<PassengerRequest> passengers;

    @Builder.Default
    private Integer holdDurationMinutes = 15;
}
