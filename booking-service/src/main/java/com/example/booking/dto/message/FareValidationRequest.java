package com.example.booking.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareValidationRequest implements Serializable {
    private String requestId;
    private Long flightId;
    private Long fareId;
    private Integer requiredSeats;
}
