package com.flight.fare.repository;

import com.flight.fare.entity.Fare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareRepository extends JpaRepository<Fare, Long> {
    Optional<Fare> findByFlightId(Long flightId);
    Optional<Fare> findFirstByFlightIdOrderByIdDesc(Long flightId);
    boolean existsByFlightId(Long flightId);
}
