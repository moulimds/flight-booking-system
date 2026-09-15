package com.flight.search.repository;

import com.flight.search.entity.FlightSearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightSearchRepository extends JpaRepository<FlightSearchIndex, Long> {
    Optional<FlightSearchIndex> findByFlightId(Long flightId);
    List<FlightSearchIndex> findBySourceIgnoreCase(String source);
    List<FlightSearchIndex> findByDestinationIgnoreCase(String destination);
    List<FlightSearchIndex> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);
    List<FlightSearchIndex> findBySourceIgnoreCaseAndDestinationIgnoreCaseAndDepartureDate(String source, String destination, LocalDate departureDate);
    void deleteByFlightId(Long flightId);
}
