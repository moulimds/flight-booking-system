package com.flight.booking.client;

import com.flight.booking.dto.SeatBookRequestDto;
import com.flight.booking.dto.SeatDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "SEAT-SERVICE")
public interface SeatClient {

    @GetMapping("/api/seats/flight/{flightId}")
    List<SeatDto> getSeatsByFlightId(@PathVariable("flightId") Long flightId);

    @PostMapping("/api/seats/{seatId}/book")
    SeatDto bookSeat(@PathVariable("seatId") Long seatId, @RequestBody SeatBookRequestDto request);

    @PostMapping("/api/seats/{seatId}/release")
    SeatDto releaseSeat(@PathVariable("seatId") Long seatId);
}
