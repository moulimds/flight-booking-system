package com.flighttracking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flight_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_leg_id", nullable = false, unique = true)
    private Long flightLegId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double altitude; // feet

    @Column(nullable = false)
    private Double speed; // knots or km/h

    @Column(nullable = false)
    private Double heading; // degrees (0-360)

    @Column(name = "distance_remaining")
    private Double distanceRemaining; // kilometers

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
