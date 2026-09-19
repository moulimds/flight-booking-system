package com.flight.checkin.controller;

import com.flight.checkin.dto.*;
import com.flight.checkin.security.UserPrincipal;
import com.flight.checkin.service.CheckInService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkins")
@PreAuthorize("hasAnyRole('ADMIN','SUPPORT','CUSTOMER','AIRPORT_ADMIN','AIRCRAFT_COMPANY_ADMIN')")
public class CheckInController {

    private static final Logger logger = LoggerFactory.getLogger(CheckInController.class);

    @Autowired
    private CheckInService checkInService;

    @GetMapping("/eligibility")
    public ResponseEntity<EligibilityResponse> checkEligibility(
            @RequestParam("bookingId") String bookingId,
            @RequestParam("passengerId") String passengerId,
            @RequestParam("flightId") String flightId,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to check check-in eligibility for booking: {}, passenger: {}, flight: {}",
                bookingId, passengerId, flightId);
        EligibilityResponse response = checkInService.checkEligibility(bookingId, passengerId, flightId, principal);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CheckInResponse> createCheckIn(
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to create check-in for booking id: {}", request.getBookingId());
        CheckInResponse response = checkInService.createCheckIn(request, principal);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckInResponse> getCheckInById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to get check-in id: {}", id);
        CheckInResponse checkIn = checkInService.getCheckInById(id, principal);
        return ResponseEntity.ok(checkIn);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<CheckInResponse> getCheckInByBookingId(
            @PathVariable("bookingId") String bookingId,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to get check-in by booking id: {}", bookingId);
        CheckInResponse checkIn = checkInService.getCheckInByBookingId(bookingId, principal);
        return ResponseEntity.ok(checkIn);
    }

    @PutMapping("/{id}/seat")
    public ResponseEntity<CheckInResponse> updateSeat(
            @PathVariable("id") String id,
            @RequestBody(required = false) SeatChangeRequest request,
            @RequestParam(value = "seatNumber", required = false) String seatNumberParam,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to change seat for check-in id: {}", id);
        CheckInResponse response = checkInService.updateSeat(id, request, seatNumberParam, principal);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/booking/{bookingId}/seat")
    public ResponseEntity<CheckInResponse> updateSeatByBooking(
            @PathVariable("bookingId") String bookingId,
            @RequestBody(required = false) SeatChangeRequest request,
            @RequestParam(value = "seatNumber", required = false) String seatNumberParam,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to change seat for booking id: {}", bookingId);
        CheckInResponse response = checkInService.updateSeat(bookingId, request, seatNumberParam, principal);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/baggage")
    public ResponseEntity<CheckInResponse> updateBaggage(
            @PathVariable("id") String id,
            @RequestBody(required = false) BaggageUpdateRequest request,
            @RequestParam(value = "baggageCount", required = false) Integer baggageCountParam,
            @RequestParam(value = "baggageWeight", required = false) Double baggageWeightParam,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to update baggage for check-in id: {}", id);
        CheckInResponse response = checkInService.updateBaggage(id, request, baggageCountParam, baggageWeightParam, principal);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/booking/{bookingId}/baggage")
    public ResponseEntity<CheckInResponse> updateBaggageByBooking(
            @PathVariable("bookingId") String bookingId,
            @RequestBody(required = false) BaggageUpdateRequest request,
            @RequestParam(value = "baggageCount", required = false) Integer baggageCountParam,
            @RequestParam(value = "baggageWeight", required = false) Double baggageWeightParam,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to update baggage for booking id: {}", bookingId);
        CheckInResponse response = checkInService.updateBaggage(bookingId, request, baggageCountParam, baggageWeightParam, principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/boarding-pass")
    public ResponseEntity<BoardingPassResponse> getBoardingPass(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to get boarding pass for check-in id: {}", id);
        BoardingPassResponse response = checkInService.getBoardingPass(id, principal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CheckInResponse> cancelCheckIn(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to cancel check-in id: {}", id);
        CheckInResponse response = checkInService.cancelCheckIn(id, principal);
        return ResponseEntity.ok(response);
    }

    // Staff / Admin access endpoints
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPPORT','AIRPORT_ADMIN','AIRCRAFT_COMPANY_ADMIN')")
    public ResponseEntity<List<CheckInResponse>> getAllCheckIns(
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to list all check-in records by staff role");
        List<CheckInResponse> list = checkInService.getAllCheckIns(principal);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/flight/{flightId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPPORT','AIRPORT_ADMIN','AIRCRAFT_COMPANY_ADMIN')")
    public ResponseEntity<List<CheckInResponse>> getCheckInsByFlight(
            @PathVariable("flightId") String flightId,
            @AuthenticationPrincipal UserPrincipal principal) {
        logger.info("REST request to get check-ins for flight: {}", flightId);
        List<CheckInResponse> list = checkInService.getCheckInsByFlightId(flightId, principal);
        return ResponseEntity.ok(list);
    }
}
