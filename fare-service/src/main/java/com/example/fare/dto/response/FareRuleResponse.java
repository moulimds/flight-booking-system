package com.example.fare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRuleResponse {
    private Long id;
    private Long fareId;
    private String baggageAllowance;
    private Boolean cancellationAllowed;
    private BigDecimal cancellationFee;
    private Boolean dateChangeAllowed;
    private BigDecimal dateChangeFee;
    private Boolean mealSelectionAllowed;
    private Boolean seatSelectionAllowed;
    private Boolean refundAllowed;
}
