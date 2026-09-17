package com.example.booking.repository;

import com.example.booking.entity.FlightSeat;
import com.example.booking.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightSeatRepository extends JpaRepository<FlightSeat, Long> {

    List<FlightSeat> findByFlightIdOrderBySeatNumberAsc(Long flightId);

    Optional<FlightSeat> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);

    List<FlightSeat> findByFlightIdAndSeatNumberIn(Long flightId, Collection<String> seatNumbers);

    List<FlightSeat> findByBookingId(Long bookingId);

    List<FlightSeat> findByFlightIdAndSeatStatus(Long flightId, SeatStatus seatStatus);
}
