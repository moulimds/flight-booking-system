package com.flighttracking.repository;

import com.flighttracking.entity.FlightLeg;
import com.flighttracking.entity.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightLegRepository extends JpaRepository<FlightLeg, Long> {

    List<FlightLeg> findByFlightIdOrderByLegNumberAsc(Long flightId);

    Optional<FlightLeg> findByFlightIdAndLegNumber(Long flightId, Integer legNumber);

    List<FlightLeg> findByStatusIn(List<FlightStatus> statuses);
}
