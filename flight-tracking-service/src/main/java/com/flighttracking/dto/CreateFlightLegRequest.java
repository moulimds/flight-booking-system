package com.flighttracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFlightLegRequest {

    private Integer legNumber;

    @NotBlank(message = "Source airport is required")
    private String source;

    @NotBlank(message = "Destination airport is required")
    private String destination;

    @NotNull(message = "Scheduled departure time is required")
    private LocalDateTime scheduledDepartureTime;

    @NotNull(message = "Scheduled arrival time is required")
    private LocalDateTime scheduledArrivalTime;
}
