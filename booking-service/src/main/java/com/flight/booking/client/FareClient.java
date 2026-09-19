package com.flight.booking.client;

import com.flight.booking.dto.FareDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "FARE-SERVICE")
public interface FareClient {

    @GetMapping("/api/fares/flight/{flightId}")
    FareDto getFareByFlightId(@PathVariable("flightId") Long flightId);
}
