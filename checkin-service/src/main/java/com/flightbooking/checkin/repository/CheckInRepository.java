package com.flightbooking.checkin.repository;

import com.flightbooking.checkin.model.CheckIn;
import com.flightbooking.checkin.model.CheckInStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    Optional<CheckIn> findByBookingIdAndPassengerId(String bookingId, String passengerId);
    List<CheckIn> findByFlightIdAndStatus(String flightId, CheckInStatus status);
    List<CheckIn> findByBookingId(String bookingId);
}
