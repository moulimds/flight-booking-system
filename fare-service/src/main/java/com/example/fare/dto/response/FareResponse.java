package com.example.fare.dto.response;

import com.example.fare.entity.CabinClass;
import com.example.fare.entity.FareStatus;
import com.example.fare.entity.FareType;
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
public class FareResponse {
    private Long id;
    private Long flightId;
    private String fareCode;
    private FareType fareType;
    private CabinClass cabinClass;
    private BigDecimal basePrice;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;
    private String currency;
    private Integer availableSeats;
    private FareStatus status;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private FareRuleResponse fareRule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
