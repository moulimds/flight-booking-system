package com.example.fare.repository;

import com.example.fare.entity.Fare;
import com.example.fare.entity.FareStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FareRepository extends JpaRepository<Fare, Long> {

    List<Fare> findByFlightId(Long flightId);

    List<Fare> findByFlightIdAndStatus(Long flightId, FareStatus status);

    List<Fare> findByFlightIdAndStatusAndAvailableSeatsGreaterThan(Long flightId, FareStatus status, int availableSeats);

    Optional<Fare> findByIdAndFlightId(Long id, Long flightId);
}
