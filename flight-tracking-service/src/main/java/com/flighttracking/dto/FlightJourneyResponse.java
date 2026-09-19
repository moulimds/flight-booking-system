package com.flighttracking.dto;

import com.flighttracking.entity.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightJourneyResponse {

    private String flightNumber;
    private LocalDateTime createdAt;
    private FlightStatus overallStatus;
    private Integer currentLeg;
    private List<FlightLegResponse> legs;
}
