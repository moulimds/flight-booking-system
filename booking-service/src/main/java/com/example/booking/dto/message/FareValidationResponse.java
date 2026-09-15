package com.example.booking.dto.message;

import com.example.booking.entity.CabinClass;
import com.example.booking.entity.FareType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareValidationResponse implements Serializable {
    private String requestId;
    private Boolean valid;
    private Long flightId;
    private Long fareId;
    private String fareCode;
    private FareType fareType;
    private CabinClass cabinClass;
    private BigDecimal basePrice;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;
    private String currency;
    private Integer availableSeats;
    private String message;
}
