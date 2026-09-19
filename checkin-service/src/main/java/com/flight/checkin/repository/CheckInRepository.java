package com.flight.checkin.repository;

import com.flight.checkin.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByBookingId(String bookingId);

    Optional<CheckIn> findFirstByBookingIdOrderByIdDesc(String bookingId);

    Optional<CheckIn> findFirstByBookingIdAndPassengerIdOrderByIdDesc(String bookingId, String passengerId);

    Optional<CheckIn> findByCheckInReference(String checkInReference);

    Optional<CheckIn> findByBoardingPassId(String boardingPassId);

    boolean existsByBookingId(String bookingId);

    boolean existsByBookingIdAndPassengerId(String bookingId, String passengerId);

    boolean existsByBookingIdAndPassengerIdAndStatus(String bookingId, String passengerId, String status);

    List<CheckIn> findByFlightId(String flightId);

    List<CheckIn> findByCustomerEmail(String customerEmail);

    Optional<CheckIn> findByFlightIdAndSeatNumberAndStatus(String flightId, String seatNumber, String status);
}
