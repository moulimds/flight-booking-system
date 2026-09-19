package com.flighttracking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flight_legs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightLeg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Flight flight;

    @Column(name = "leg_number", nullable = false)
    private Integer legNumber;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String destination;

    @Column(name = "scheduled_departure_time", nullable = false)
    private LocalDateTime scheduledDepartureTime;

    @Column(name = "scheduled_arrival_time", nullable = false)
    private LocalDateTime scheduledArrivalTime;

    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime;

    @Column(name = "estimated_arrival_time")
    private LocalDateTime estimatedArrivalTime;

    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;
}
