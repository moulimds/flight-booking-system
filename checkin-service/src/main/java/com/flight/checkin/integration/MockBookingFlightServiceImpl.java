package com.flight.checkin.integration;

import com.flight.checkin.exception.InvalidCheckInException;
import com.flight.checkin.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MockBookingFlightServiceImpl implements BookingFlightService {

    private static final Logger logger = LoggerFactory.getLogger(MockBookingFlightServiceImpl.class);

    private final Map<String, BookingDetails> mockBookings = new ConcurrentHashMap<>();
    private final Map<String, FlightDetails> mockFlights = new ConcurrentHashMap<>();

    public MockBookingFlightServiceImpl() {
        initializeTestData();
    }

    private void initializeTestData() {
        
        BookingDetails booking1 = new BookingDetails(
                "BK1001",
                "P1001",
                "John Customer",
                "FL1001",
                "customer@example.com",
                "CONFIRMED",
                "SUCCESS",
                "12A"
        );
        mockBookings.put("BK1001", booking1);
        mockBookings.put("1", new BookingDetails(
                "1",
                "1",
                "John Customer",
                "1",
                "customer@example.com",
                "CONFIRMED",
                "SUCCESS",
                "12A"
        ));
        mockBookings.put("B1001", booking1);

        // Second valid booking for another passenger/customer
        mockBookings.put("BK1002", new BookingDetails(
                "BK1002",
                "P1002",
                "Jane Doe",
                "FL1001",
                "other_customer@example.com",
                "CONFIRMED",
                "SUCCESS",
                "12B"
        ));

        // Negative test data: Cancelled booking
        mockBookings.put("BK_CANCELLED", new BookingDetails(
                "BK_CANCELLED",
                "P1001",
                "John Customer",
                "FL1001",
                "customer@example.com",
                "CANCELLED",
                "SUCCESS",
                "14A"
        ));

        // Negative test data: Unpaid booking
        mockBookings.put("BK_UNPAID", new BookingDetails(
                "BK_UNPAID",
                "P1001",
                "John Customer",
                "FL1001",
                "customer@example.com",
                "CONFIRMED",
                "PENDING",
                "14B"
        ));

        
        mockBookings.put("BK_DEPARTED", new BookingDetails(
                "BK_DEPARTED",
                "P1001",
                "John Customer",
                "FL_DEPARTED",
                "customer@example.com",
                "CONFIRMED",
                "SUCCESS",
                "15A"
        ));

 
        mockBookings.put("BK_CLOSED", new BookingDetails(
                "BK_CLOSED",
                "P1001",
                "John Customer",
                "FL_CLOSED",
                "customer@example.com",
                "CONFIRMED",
                "SUCCESS",
                "15B"
        ));

       
        FlightDetails flight1 = new FlightDetails(
                "FL1001",
                "AI101",
                "DEL",
                "BOM",
                "SCHEDULED",
                LocalDateTime.now().plusHours(24),
                "G12",
                "T2",
                true
        );
        mockFlights.put("FL1001", flight1);
        mockFlights.put("1", flight1);

      
        mockFlights.put("FL_DEPARTED", new FlightDetails(
                "FL_DEPARTED",
                "AI102",
                "DEL",
                "BOM",
                "DEPARTED",
                LocalDateTime.now().minusHours(2),
                "G14",
                "T2",
                false
        ));

        mockFlights.put("FL_CLOSED", new FlightDetails(
                "FL_CLOSED",
                "AI103",
                "DEL",
                "BLR",
                "SCHEDULED",
                LocalDateTime.now().plusDays(7),
                "G15",
                "T1",
                false
        ));
    }

    @Override
    public BookingDetails getBooking(String bookingId) {
        return mockBookings.get(bookingId);
    }

    @Override
    public FlightDetails getFlight(String flightId) {
        return mockFlights.get(flightId);
    }

    @Override
    public void validateEligibility(String bookingId, String passengerId, String flightId, String userEmail, boolean isCustomer) {
        BookingDetails booking = mockBookings.get(bookingId);
        if (booking == null) {
            throw new ResourceNotFoundException("Booking not found with ID: " + bookingId);
        }

        if (isCustomer && userEmail != null && !userEmail.equalsIgnoreCase(booking.getCustomerEmail())) {
            throw new InvalidCheckInException("Access Denied: Booking " + bookingId + " does not belong to passenger account " + userEmail);
        }

        if (!"CONFIRMED".equalsIgnoreCase(booking.getBookingStatus())) {
            throw new InvalidCheckInException("Cannot check in: Booking status is " + booking.getBookingStatus() + ". Only CONFIRMED bookings can check in.");
        }

        if (!"SUCCESS".equalsIgnoreCase(booking.getPaymentStatus())) {
            throw new InvalidCheckInException("Cannot check in: Payment status is " + booking.getPaymentStatus() + ". Payment must be SUCCESS.");
        }

        if (!booking.getPassengerId().equalsIgnoreCase(passengerId)) {
            throw new InvalidCheckInException("Passenger ID '" + passengerId + "' does not match booking record.");
        }

        FlightDetails flight = mockFlights.get(flightId);
        if (flight == null) {
            throw new ResourceNotFoundException("Flight not found with ID: " + flightId);
        }

        if (flight.getDepartureTime().isBefore(LocalDateTime.now()) || "DEPARTED".equalsIgnoreCase(flight.getStatus())) {
            throw new InvalidCheckInException("Cannot check in: Flight " + flightId + " has already departed.");
        }

        if (!flight.isCheckInWindowOpen()) {
            throw new InvalidCheckInException("Cannot check in: Check-in window is currently closed for flight " + flightId + ".");
        }
    }

    @Override
    public void notifySeatCheckedIn(String flightId, String seatNumber) {
        logger.info("Mock: Seat {} on flight {} marked as CHECKED_IN", seatNumber, flightId);
    }

    @Override
    public void notifySeatReleased(String flightId, String seatNumber) {
        logger.info("Mock: Seat {} on flight {} released", seatNumber, flightId);
    }
}
