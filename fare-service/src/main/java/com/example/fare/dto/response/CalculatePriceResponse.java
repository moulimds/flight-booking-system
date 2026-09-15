package com.example.fare.dto.response;

import com.example.fare.entity.CabinClass;
import com.example.fare.entity.FareType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculatePriceResponse {

    private Long fareId;
    private Long flightId;
    private String fareCode;
    private FareType fareType;
    private CabinClass cabinClass;
    private Integer passengerCount;
    private BigDecimal basePricePerPassenger;
    private BigDecimal taxPerPassenger;
    private BigDecimal totalPerPassenger;
    private BigDecimal totalBasePrice;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;
    private String currency;
    private Integer availableSeats;
}
