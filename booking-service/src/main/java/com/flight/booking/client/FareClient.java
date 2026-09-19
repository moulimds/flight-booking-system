package com.flight.booking.client;

import com.flight.booking.dto.FareDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "FARE-SERVICE")
public interface FareClient {

    @GetMapping("/api/fares/flight/{flightId}")
    List<FareDto> getFaresByFlightId(@PathVariable("flightId") Long flightId);
}
