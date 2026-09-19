package com.flight.seat.repository;

import com.flight.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByFlightId(Long flightId);
    List<Seat> findByFlightIdAndStatus(Long flightId, String status);
    Optional<Seat> findFirstByFlightIdAndSeatNumberOrderByIdDesc(Long flightId, String seatNumber);
    Optional<Seat> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);
    Optional<Seat> findByBookingId(Long bookingId);
    boolean existsByFlightIdAndSeatNumber(Long flightId, String seatNumber);
}
