package com.airline.seat.repository;

import com.airline.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByFlightId(String flightId);

    Optional<Seat> findByFlightIdAndSeatNumber(String flightId, String seatNumber);

    List<Seat> findByIdIn(List<Long> ids);
}
