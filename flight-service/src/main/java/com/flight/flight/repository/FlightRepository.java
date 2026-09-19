package com.flight.flight.repository;

import com.flight.flight.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    Optional<Flight> findByFlightNumber(String flightNumber);
    boolean existsByFlightNumber(String flightNumber);
    List<Flight> findBySourceIgnoreCase(String source);
    List<Flight> findByDestinationIgnoreCase(String destination);
    List<Flight> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);
    List<Flight> findBySourceIgnoreCaseAndDestinationIgnoreCaseAndDepartureDate(String source, String destination, LocalDate departureDate);
}
