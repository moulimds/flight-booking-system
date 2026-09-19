package com.flight.seat.service;

import com.flight.seat.dto.*;

import java.util.List;

public interface SeatService {
    SeatResponse createSeat(SeatRequest request);
    List<SeatResponse> createBatchSeats(BatchSeatRequest request);
    List<SeatResponse> getAllSeats();
    SeatResponse getSeatById(Long id);
    List<SeatResponse> getSeatsByFlightId(Long flightId);
    List<SeatResponse> getAvailableSeatsByFlightId(Long flightId);
    SeatResponse updateSeat(Long id, SeatRequest request);
    void deleteSeat(Long id);
    SeatResponse holdSeat(Long seatId, SeatHoldRequest request);
    SeatResponse bookSeat(Long seatId, SeatBookRequest request);
    SeatResponse releaseSeat(Long seatId);
    void releaseSeatByBookingId(Long bookingId);
    void markSeatCheckedIn(Long flightId, String seatNumber);
}
