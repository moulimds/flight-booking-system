package com.flight.search.client;

import com.flight.search.dto.FlightDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "FLIGHT-SERVICE")
public interface FlightClient {

    @GetMapping("/api/flights")
    List<FlightDto> getAllFlights();

    @GetMapping("/api/flights/{id}")
    FlightDto getFlightById(@PathVariable("id") Long id);
}
