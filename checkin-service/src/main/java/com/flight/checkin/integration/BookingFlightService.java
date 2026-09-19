package com.flight.checkin.integration;

public interface BookingFlightService {

    BookingDetails getBooking(String bookingId);

    FlightDetails getFlight(String flightId);

    void validateEligibility(String bookingId, String passengerId, String flightId, String userEmail, boolean isCustomer);

    void notifySeatCheckedIn(String flightId, String seatNumber);

    void notifySeatReleased(String flightId, String seatNumber);
}
