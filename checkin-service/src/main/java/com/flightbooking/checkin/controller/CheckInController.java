package com.flightbooking.checkin.controller;

import com.flightbooking.checkin.dto.*;
import com.flightbooking.checkin.model.CheckIn;
import com.flightbooking.checkin.service.CheckInService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkins")
public class CheckInController {
    private final CheckInService service;

    public CheckInController(CheckInService service) { this.service = service; }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("service", "checkin-service", "status", "UP"); }

    @GetMapping("/eligibility")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT','OPERATOR')")
    public Map<String, String> eligibility(
            @RequestParam String bookingId,
            @RequestParam String passengerId,
            @RequestParam String flightId) {
        return Map.of("result", service.eligibility(bookingId, passengerId, flightId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT','OPERATOR')")
    public CheckIn create(@Valid @RequestBody CheckInRequest request, Authentication auth) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT','OPERATOR')")
    public CheckIn get(@PathVariable Long id) { return service.get(id); }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT','OPERATOR')")
    public java.util.List<CheckIn> booking(@PathVariable String bookingId) { return service.byBooking(bookingId); }

    @PutMapping("/{id}/seat")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT','OPERATOR')")
    public CheckIn seat(@PathVariable Long id, @Valid @RequestBody SeatRequest request) {
        return service.seat(id, request);
    }

    @PutMapping("/{id}/baggage")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT','OPERATOR')")
    public CheckIn baggage(@PathVariable Long id, @Valid @RequestBody BaggageRequest request) {
        return service.baggage(id, request);
    }

    @GetMapping("/{id}/boarding-pass")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT','OPERATOR')")
    public Map<String, Object> boardingPass(@PathVariable Long id) {
        CheckIn c = service.get(id);
        return Map.of(
            "boardingPassId", "BP-" + c.getId(),
            "bookingId", c.getBookingId(),
            "passengerId", c.getPassengerId(),
            "flightId", c.getFlightId(),
            "seatNumber", c.getSeatNumber() == null ? "TBD" : c.getSeatNumber(),
            "status", c.getStatus().name(),
            "issuedAt", c.getCheckedInAt()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT','OPERATOR')")
    public CheckIn cancel(@PathVariable Long id) { return service.cancel(id); }

    @GetMapping("/flight/{flightId}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT','OPERATOR')")
    public java.util.List<CheckIn> flight(@PathVariable String flightId) {
        return service.flight(flightId);
    }

    @PostMapping("/{id}/override")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT','OPERATOR')")
    public CheckIn override(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/flight/{flightId}/close")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public Map<String, String> close(@PathVariable String flightId) {
        return Map.of("flightId", flightId, "message", "Check-in closure endpoint placeholder; connect to flight schedule service.");
    }
}
