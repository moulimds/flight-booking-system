package com.example.booking.dto.response;

import com.example.booking.entity.CabinClass;
import com.example.booking.entity.SeatStatus;
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
public class FlightSeatResponse {

    private Long id;
    private Long flightId;
    private String seatNumber;
    private CabinClass cabinClass;
    private SeatStatus seatStatus;
    private Boolean isAvailable;
    private Long bookingId;
    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime holdExpiresAt;

    private String blockedReason;
}
