package com.flight.search.client;

import com.flight.search.dto.FareDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "FARE-SERVICE")
public interface FareClient {

    @GetMapping("/api/fares/flight/{flightId}")
    FareDto getFareByFlightId(@PathVariable("flightId") Long flightId);
}
