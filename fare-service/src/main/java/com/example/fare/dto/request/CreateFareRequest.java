package com.example.fare.dto.request;

import com.example.fare.entity.CabinClass;
import com.example.fare.entity.FareType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFareRequest {

    @NotNull(message = "Flight ID is required")
    @Positive(message = "Flight ID must be positive")
    private Long flightId;

    @NotBlank(message = "Fare code is required")
    @Size(max = 50, message = "Fare code must not exceed 50 characters")
    private String fareCode;

    @NotNull(message = "Fare type is required")
    private FareType fareType;

    @NotNull(message = "Cabin class is required")
    private CabinClass cabinClass;

    @NotNull(message = "Base price is required")
    @PositiveOrZero(message = "Base price must be positive or zero")
    private BigDecimal basePrice;

    @NotNull(message = "Tax amount is required")
    @PositiveOrZero(message = "Tax amount must be positive or zero")
    private BigDecimal taxAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

    @NotNull(message = "Available seats is required")
    @Min(value = 0, message = "Available seats cannot be negative")
    private Integer availableSeats;

    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid until date is required")
    @Future(message = "Valid until date must be in the future")
    private LocalDateTime validUntil;

    // Fare Rules
    private String baggageAllowance;
    private Boolean cancellationAllowed;
    private BigDecimal cancellationFee;
    private Boolean dateChangeAllowed;
    private BigDecimal dateChangeFee;
    private Boolean mealSelectionAllowed;
    private Boolean seatSelectionAllowed;
    private Boolean refundAllowed;
}
