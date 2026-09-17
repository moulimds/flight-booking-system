package com.example.booking.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent implements Serializable {
    private String eventId;
    private Long bookingId;
    private String pnr;
    private String bookingReference;
    private Long userId;
    private Long flightId;
    private Long fareId;
    private String fareCode;
    private BigDecimal totalAmount;
    private String currency;
    private Integer passengerCount;
    private LocalDateTime timestamp;
}
