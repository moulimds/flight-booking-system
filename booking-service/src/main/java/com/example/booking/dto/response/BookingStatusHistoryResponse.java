package com.example.booking.dto.response;

import com.example.booking.entity.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusHistoryResponse {
    private Long id;
    private Long bookingId;
    private BookingStatus previousStatus;
    private BookingStatus newStatus;
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime changedAt;
}
