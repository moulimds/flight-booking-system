package com.flight.booking.repository;

import com.flight.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);
    Optional<Booking> findFirstByFlightIdAndSeatNumberOrderByIdDesc(Long flightId, String seatNumber);
    List<Booking> findAllByFlightIdAndSeatNumber(Long flightId, String seatNumber);
    Optional<Booking> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByFlightId(Long flightId);
    boolean existsByBookingReference(String bookingReference);
}
