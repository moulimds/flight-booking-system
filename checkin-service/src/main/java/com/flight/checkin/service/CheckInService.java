package com.flight.checkin.service;

import com.flight.checkin.dto.*;
import com.flight.checkin.security.UserPrincipal;

import java.util.List;

public interface CheckInService {

    EligibilityResponse checkEligibility(String bookingId, String passengerId, String flightId, UserPrincipal principal);

    CheckInResponse createCheckIn(CheckInRequest request, UserPrincipal principal);

    CheckInResponse getCheckInById(Long id, UserPrincipal principal);

    CheckInResponse getCheckInById(String identifier, UserPrincipal principal);

    CheckInResponse getCheckInByBookingId(String bookingId, UserPrincipal principal);

    CheckInResponse updateSeat(Long id, SeatChangeRequest request, UserPrincipal principal);

    CheckInResponse updateSeat(String identifier, SeatChangeRequest request, String seatParam, UserPrincipal principal);

    CheckInResponse updateBaggage(Long id, BaggageUpdateRequest request, UserPrincipal principal);

    CheckInResponse updateBaggage(String identifier, BaggageUpdateRequest request, Integer countParam, Double weightParam, UserPrincipal principal);

    BoardingPassResponse getBoardingPass(Long id, UserPrincipal principal);

    BoardingPassResponse getBoardingPass(String identifier, UserPrincipal principal);

    CheckInResponse cancelCheckIn(Long id, UserPrincipal principal);

    CheckInResponse cancelCheckIn(String identifier, UserPrincipal principal);

    List<CheckInResponse> getAllCheckIns(UserPrincipal principal);

    List<CheckInResponse> getCheckInsByFlightId(String flightId, UserPrincipal principal);
}
