package com.flight.checkin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SEAT-SERVICE")
public interface SeatClient {

    @PatchMapping("/api/seats/flight/{flightId}/seat/{seatNumber}/checkin")
    void markSeatCheckedIn(@PathVariable("flightId") Long flightId, @PathVariable("seatNumber") String seatNumber);
}
