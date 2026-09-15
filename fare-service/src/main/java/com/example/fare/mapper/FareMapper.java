package com.example.fare.mapper;

import com.example.fare.dto.request.CreateFareRequest;
import com.example.fare.dto.response.FareResponse;
import com.example.fare.dto.response.FareRuleResponse;
import com.example.fare.entity.Fare;
import com.example.fare.entity.FareRule;
import com.example.fare.entity.FareStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FareMapper {

    public Fare toEntity(CreateFareRequest request) {
        BigDecimal base = request.getBasePrice();
        BigDecimal tax = request.getTaxAmount();
        BigDecimal total = (base != null && tax != null) ? base.add(tax) : BigDecimal.ZERO;

        Fare fare = Fare.builder()
                .flightId(request.getFlightId())
                .fareCode(request.getFareCode())
                .fareType(request.getFareType())
                .cabinClass(request.getCabinClass())
                .basePrice(base)
                .taxAmount(tax)
                .totalPrice(total)
                .currency(request.getCurrency())
                .availableSeats(request.getAvailableSeats())
                .status(FareStatus.ACTIVE)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .build();

        FareRule rule = FareRule.builder()
                .baggageAllowance(request.getBaggageAllowance() != null ? request.getBaggageAllowance() : "15 kg Check-in, 7 kg Cabin")
                .cancellationAllowed(request.getCancellationAllowed() != null ? request.getCancellationAllowed() : true)
                .cancellationFee(request.getCancellationFee() != null ? request.getCancellationFee() : BigDecimal.valueOf(1500.00))
                .dateChangeAllowed(request.getDateChangeAllowed() != null ? request.getDateChangeAllowed() : true)
                .dateChangeFee(request.getDateChangeFee() != null ? request.getDateChangeFee() : BigDecimal.valueOf(1000.00))
                .mealSelectionAllowed(request.getMealSelectionAllowed() != null ? request.getMealSelectionAllowed() : false)
                .seatSelectionAllowed(request.getSeatSelectionAllowed() != null ? request.getSeatSelectionAllowed() : true)
                .refundAllowed(request.getRefundAllowed() != null ? request.getRefundAllowed() : true)
                .build();

        fare.setFareRule(rule);
        return fare;
    }

    public FareResponse toResponse(Fare fare) {
        if (fare == null) {
            return null;
        }

        FareRuleResponse ruleResponse = null;
        if (fare.getFareRule() != null) {
            FareRule rule = fare.getFareRule();
            ruleResponse = FareRuleResponse.builder()
                    .id(rule.getId())
                    .fareId(rule.getFareId() != null ? rule.getFareId() : fare.getId())
                    .baggageAllowance(rule.getBaggageAllowance())
                    .cancellationAllowed(rule.getCancellationAllowed())
                    .cancellationFee(rule.getCancellationFee())
                    .dateChangeAllowed(rule.getDateChangeAllowed())
                    .dateChangeFee(rule.getDateChangeFee())
                    .mealSelectionAllowed(rule.getMealSelectionAllowed())
                    .seatSelectionAllowed(rule.getSeatSelectionAllowed())
                    .refundAllowed(rule.getRefundAllowed())
                    .build();
        }

        return FareResponse.builder()
                .id(fare.getId())
                .flightId(fare.getFlightId())
                .fareCode(fare.getFareCode())
                .fareType(fare.getFareType())
                .cabinClass(fare.getCabinClass())
                .basePrice(fare.getBasePrice())
                .taxAmount(fare.getTaxAmount())
                .totalPrice(fare.getTotalPrice())
                .currency(fare.getCurrency())
                .availableSeats(fare.getAvailableSeats())
                .status(fare.getStatus())
                .validFrom(fare.getValidFrom())
                .validUntil(fare.getValidUntil())
                .fareRule(ruleResponse)
                .createdAt(fare.getCreatedAt())
                .updatedAt(fare.getUpdatedAt())
                .version(fare.getVersion())
                .build();
    }
}
