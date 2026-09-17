package com.example.booking.dto.response;

import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.CabinClass;
import com.example.booking.entity.FareType;
import com.example.booking.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private String pnr;
    private Long userId;
    private Long flightId;
    private Long fareId;

    // Fare Price Snapshot
    private String fareCode;
    private FareType fareType;
    private CabinClass cabinClass;
    private BigDecimal basePrice;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currency;

    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;

    private BigDecimal cancellationFee;
    private BigDecimal refundAmount;
    private String cancellationReason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime holdExpiresAt;

    private String blockedReason;

    private PriceBreakdownResponse priceBreakdown;

    private List<PassengerResponse> passengers;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long version;
}
