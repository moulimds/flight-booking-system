package com.flighttracking.repository;

import com.flighttracking.entity.FlightTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlightTrackingRepository extends JpaRepository<FlightTracking, Long> {

    Optional<FlightTracking> findByFlightLegId(Long flightLegId);

    void deleteByFlightLegId(Long flightLegId);
}
