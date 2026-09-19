package com.flight.checkin.client;

import com.flight.checkin.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BOOKING-SERVICE")
public interface BookingClient {

    @GetMapping("/api/bookings/{id}")
    BookingDto getBookingById(@PathVariable("id") Long id);
}
