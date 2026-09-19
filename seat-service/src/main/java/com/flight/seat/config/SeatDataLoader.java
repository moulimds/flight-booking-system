package com.flight.seat.config;

import com.flight.seat.entity.Seat;
import com.flight.seat.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SeatDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(SeatDataLoader.class);

    @Bean
    public CommandLineRunner loadSampleSeats(SeatRepository seatRepository) {
        return args -> {
            if (seatRepository.count() == 0) {
                logger.info("Seeding sample seats (20 per flight) into flight_seat_db...");

                List<Seat> seats = new ArrayList<>();
                String[] cols = {"A", "B", "C", "D"};

                // Seed 20 seats (rows 1 to 5) for 6 flights (flight IDs 1 to 6)
                for (long flightId = 1; flightId <= 6; flightId++) {
                    for (int row = 1; row <= 5; row++) {
                        String seatClass = (row == 1) ? "BUSINESS" : "ECONOMY";
                        for (String col : cols) {
                            String seatNum = row + col;
                            seats.add(new Seat(null, flightId, seatNum, seatClass, "AVAILABLE", null));
                        }
                    }
                }

                // Dedicated seat allocated exclusively for DELETE endpoint
                seats.add(new Seat(null, 1L, "99Z", "ECONOMY", "AVAILABLE", null));

                seatRepository.saveAll(seats);
                logger.info("Successfully seeded {} sample seats (including dedicated delete target seat 99Z).", seats.size());
            }

            // Ensure dedicated seat for DELETE endpoint always exists
            if (seatRepository.findByFlightIdAndSeatNumber(1L, "99Z").isEmpty()) {
                seatRepository.save(new Seat(null, 1L, "99Z", "ECONOMY", "AVAILABLE", null));
                logger.info("Ensured dedicated delete seat 99Z exists.");
            }
        };
    }
}
