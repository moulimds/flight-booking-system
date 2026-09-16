package com.flightbooking.checkin.service;

import com.flightbooking.checkin.dto.*;
import com.flightbooking.checkin.model.*;
import com.flightbooking.checkin.repository.CheckInRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CheckInService {
    private final CheckInRepository repo;

    public CheckInService(CheckInRepository repo) { this.repo = repo; }

    public String eligibility(String bookingId, String passengerId, String flightId) {
        var existing = repo.findByBookingIdAndPassengerId(bookingId, passengerId);
        if (existing.isPresent() && existing.get().getStatus() == CheckInStatus.CHECKED_IN)
            return "NOT_ELIGIBLE: Passenger is already checked in";
        // Integration point: call Booking Service and Flight Service here in the complete system.
        return "ELIGIBLE: Booking confirmed and check-in window validation should be performed by the integrated services";
    }

    public CheckIn create(CheckInRequest r) {
        var existing = repo.findByBookingIdAndPassengerId(r.bookingId(), r.passengerId());
        if (existing.isPresent() && existing.get().getStatus() == CheckInStatus.CHECKED_IN)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Passenger already checked in");

        CheckIn c = existing.orElseGet(CheckIn::new);
        c.setBookingId(r.bookingId());
        c.setPassengerId(r.passengerId());
        c.setFlightId(r.flightId());
        c.setSeatNumber(r.seatNumber());
        c.setBaggageCount(r.baggageCount() == null ? 0 : r.baggageCount());
        c.setBaggageWeight(r.baggageWeight() == null ? 0.0 : r.baggageWeight());
        c.setStatus(CheckInStatus.CHECKED_IN);
        c.setCheckedInAt(LocalDateTime.now());
        return repo.save(c);
    }

    public CheckIn get(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Check-in not found"));
    }

    public List<CheckIn> byBooking(String bookingId) { return repo.findByBookingId(bookingId); }

    public CheckIn seat(Long id, SeatRequest r) {
        CheckIn c = get(id);
        ensureActive(c);
        c.setSeatNumber(r.seatNumber());
        return repo.save(c);
    }

    public CheckIn baggage(Long id, BaggageRequest r) {
        CheckIn c = get(id);
        ensureActive(c);
        c.setBaggageCount(r.baggageCount());
        c.setBaggageWeight(r.baggageWeight());
        return repo.save(c);
    }

    public CheckIn cancel(Long id) {
        CheckIn c = get(id);
        c.setStatus(CheckInStatus.CANCELLED);
        return repo.save(c);
    }

    public List<CheckIn> flight(String flightId) {
        return repo.findByFlightIdAndStatus(flightId, CheckInStatus.CHECKED_IN);
    }

    private void ensureActive(CheckIn c) {
        if (c.getStatus() != CheckInStatus.CHECKED_IN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in is not active");
    }
}
