package com.example.booking.service;

import com.example.booking.dto.request.CancelBookingRequest;
import com.example.booking.dto.request.CreateBookingRequest;
import com.example.booking.dto.response.BookingCancellationResponse;
import com.example.booking.dto.response.BookingResponse;
import com.example.booking.dto.response.BookingStatusHistoryResponse;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request, String idempotencyKey);

    BookingResponse getBookingById(Long bookingId);

    BookingResponse getBookingByPnr(String pnr);

    List<BookingResponse> getBookingsByUserId(Long userId);

    List<BookingStatusHistoryResponse> getBookingStatusHistory(Long bookingId);

    BookingCancellationResponse cancelBooking(Long bookingId, CancelBookingRequest request);

    BookingResponse confirmBooking(Long bookingId);

    List<BookingResponse> getAllBookings(Long userId);

    BookingResponse holdBooking(com.example.booking.dto.request.HoldBookingRequest request);

    BookingResponse releaseBookingHold(Long bookingId);

    List<com.example.booking.dto.response.FlightSeatResponse> getFlightSeats(Long flightId);

    List<com.example.booking.dto.response.FlightSeatResponse> adminBlockSeats(com.example.booking.dto.request.AdminBlockSeatsRequest request);

    List<com.example.booking.dto.response.FlightSeatResponse> adminUnblockSeats(com.example.booking.dto.request.AdminUnblockSeatsRequest request);

    BookingResponse adminBlockBooking(Long bookingId, com.example.booking.dto.request.AdminBlockBookingRequest request);

    BookingResponse adminUnblockBooking(Long bookingId);
}
