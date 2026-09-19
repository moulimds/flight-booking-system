package com.flighttracking.airport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportCoordinate {
    private String name;
    private Double latitude;
    private Double longitude;
}
