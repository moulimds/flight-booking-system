package com.airline.seat.service;

import com.airline.seat.dto.*;

import java.util.List;

public interface SeatService {

    List<SeatResponse> holdSeats(SeatHoldRequest request);

    List<SeatResponse> reserveSeats(SeatReserveRequest request);

    List<SeatResponse> releaseSeats(SeatReleaseRequest request);

    List<SeatResponse> blockSeats(SeatBlockRequest request);

    List<SeatResponse> unblockSeats(SeatUnblockRequest request);

    SeatResponse changeSeat(SeatChangeRequest request);

    AvailableSeatsCountResponse getAvailableSeatsCount(String flightScheduleId);

    FlightSeatMapResponse getFlightSeatMap(String flightId, String flightScheduleId);

    void releaseExpiredHolds();
}
