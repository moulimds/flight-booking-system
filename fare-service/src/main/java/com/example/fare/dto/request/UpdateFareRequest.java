package com.example.fare.dto.request;

import com.example.fare.entity.CabinClass;
import com.example.fare.entity.FareType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
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
public class UpdateFareRequest {

    private String fareCode;
    private FareType fareType;
    private CabinClass cabinClass;

    @PositiveOrZero(message = "Base price must be positive or zero")
    private BigDecimal basePrice;

    @PositiveOrZero(message = "Tax amount must be positive or zero")
    private BigDecimal taxAmount;

    private String currency;

    @Min(value = 0, message = "Available seats cannot be negative")
    private Integer availableSeats;

    private LocalDateTime validFrom;
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
