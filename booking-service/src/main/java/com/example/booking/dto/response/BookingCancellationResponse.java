package com.example.booking.dto.response;

import com.example.booking.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancellationResponse {
    private String bookingReference;
    private String pnr;
    private BigDecimal originalAmount;
    private BigDecimal cancellationFee;
    private BigDecimal refundAmount;
    private String currency;
    private BookingStatus status;
    private String reason;
}
