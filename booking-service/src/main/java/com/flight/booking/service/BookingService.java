package com.flight.booking.service;

import com.flight.booking.dto.BookingRequest;
import com.flight.booking.dto.BookingResponse;
import com.flight.booking.dto.BookingUpdateRequest;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    List<BookingResponse> getAllBookings();
    BookingResponse getBookingById(Long id);
    BookingResponse getBookingByReference(String reference);
    List<BookingResponse> getBookingsByUserId(Long userId);
    List<BookingResponse> getBookingsByFlightId(Long flightId);
    BookingResponse updateBooking(Long id, BookingUpdateRequest request);
    void deleteBooking(Long id);
    default BookingResponse cancelBooking(Long id) {
        return cancelBooking(id, null);
    }
    BookingResponse cancelBooking(Long id, Integer hoursBeforeDeparture);
    BookingResponse confirmBooking(Long id, Long paymentId);
    BookingResponse approveReschedule(Long id, BookingUpdateRequest request);

    BookingResponse rejectReschedule(Long id, BookingUpdateRequest request);
    BookingResponse rescheduleBooking(Long id, BookingUpdateRequest request);
    void handlePaymentSuccess(Long bookingId, Long paymentId);
    void handlePaymentFailure(Long bookingId, String reason);
    void handleCheckInCompleted(Long bookingId);
}
