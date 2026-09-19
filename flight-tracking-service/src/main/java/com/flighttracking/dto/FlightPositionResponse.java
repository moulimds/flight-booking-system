package com.flighttracking.dto;

import com.flighttracking.entity.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightPositionResponse {

    private String flightNumber;
    private Integer legNumber;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;
    private Double heading;
    private Double distanceRemaining;
    private FlightStatus status;
    private LocalDateTime updatedAt;
}
