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
public class FlightTrackingResponse {

    private String flightNumber;
    private LocalDateTime createdAt;
    private FlightStatus overallStatus;
    private Integer currentLeg;
    private String source;
    private String destination;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;
    private Double heading;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime actualDeparture;
    private LocalDateTime scheduledArrival;
    private LocalDateTime estimatedArrival;
    private LocalDateTime actualArrival;
    private LocalDateTime updatedAt;
}
