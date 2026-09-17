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
public class BookingCancelledEvent implements Serializable {
    private String eventId;
    private Long bookingId;
    private String pnr;
    private String bookingReference;
    private BigDecimal originalAmount;
    private BigDecimal cancellationFee;
    private BigDecimal refundAmount;
    private String currency;
    private String reason;
    private LocalDateTime timestamp;
}
