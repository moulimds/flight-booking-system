package com.flight.booking.config;

import com.flight.booking.entity.Booking;
import com.flight.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Configuration
public class BookingDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(BookingDataLoader.class);

    @Bean
    public CommandLineRunner loadSampleBookings(BookingRepository bookingRepository) {
        return args -> {
            Optional<Booking> existingRef = bookingRepository.findByBookingReference("BK1001");
            if (existingRef.isEmpty()) {
                logger.info("Seeding sample booking BK1001 into flight_booking_db...");

                Booking b = new Booking(
                        null,
                        "BK1001",
                        1L,
                        "PNR1001",
                        1L,
                        "John Customer",
                        "customer@example.com",
                        "+919876543210",
                        LocalDate.of(1995, 5, 20),
                        "MALE",
                        "12A",
                        new BigDecimal("5000.00"),
                        "CONFIRMED",
                        "SUCCESS",
                        "NOT_CHECKED_IN"
                );
                bookingRepository.save(b);
                logger.info("Successfully seeded sample booking BK1001.");
            }

            // Dedicated booking allocated exclusively for DELETE endpoint
            Optional<Booking> deleteBooking = bookingRepository.findByBookingReference("BK-DELETE-99");
            if (deleteBooking.isEmpty() && bookingRepository.findByBookingReference("BK9999").isEmpty()) {
                Booking bDel = new Booking(
                        null,
                        "BK-DELETE-99",
                        1L,
                        "PNR9999",
                        1L,
                        "Delete Test Passenger",
                        "delete_test@example.com",
                        "+919876543210",
                        LocalDate.of(1990, 1, 1),
                        "MALE",
                        "20D",
                        new BigDecimal("4500.00"),
                        "CONFIRMED",
                        "SUCCESS",
                        "NOT_CHECKED_IN"
                );
                bookingRepository.save(bDel);
                logger.info("Successfully seeded dedicated delete booking BK-DELETE-99.");
            }
        };
    }
}
