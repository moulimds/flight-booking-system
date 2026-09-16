package com.airline.seat.repository;

import com.airline.seat.entity.SeatInventory;
import com.airline.seat.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {

    List<SeatInventory> findByFlightScheduleId(String flightScheduleId);

    Optional<SeatInventory> findByFlightScheduleIdAndSeatId(String flightScheduleId, Long seatId);

    List<SeatInventory> findByFlightScheduleIdAndSeatIdIn(String flightScheduleId, List<Long> seatIds);

    long countByFlightScheduleIdAndStatus(String flightScheduleId, SeatStatus status);

    List<SeatInventory> findByStatusAndHoldExpiresAtBefore(SeatStatus status, LocalDateTime dateTime);

    @Query("SELECT si FROM SeatInventory si JOIN si.seat s WHERE s.flightId = :flightId AND si.flightScheduleId = :flightScheduleId")
    List<SeatInventory> findByFlightIdAndScheduleId(@Param("flightId") String flightId, @Param("flightScheduleId") String flightScheduleId);
}
