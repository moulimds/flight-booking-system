package com.flight.booking.client;

import com.flight.booking.dto.FlightDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "FLIGHT-SERVICE")
public interface FlightClient {

    @GetMapping("/api/flights/{id}")
    FlightDto getFlightById(@PathVariable("id") Long id);
}
