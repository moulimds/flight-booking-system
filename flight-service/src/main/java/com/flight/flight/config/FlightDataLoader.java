package com.flight.flight.config;

import com.flight.flight.entity.Flight;
import com.flight.flight.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

@Configuration
public class FlightDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(FlightDataLoader.class);

    @Bean
    public CommandLineRunner loadSampleFlights(FlightRepository flightRepository) {
        return args -> {
            if (flightRepository.count() == 0) {
                logger.info("Seeding sample flights into flight_db...");

                LocalDate flightDate = LocalDate.now().plusDays(7); // Next week

                Flight f1 = new Flight(
                        null,
                        "AI101",
                        "Air India",
                        "CHENNAI",
                        "DELHI",
                        flightDate,
                        LocalTime.of(6, 0),
                        flightDate,
                        LocalTime.of(8, 45),
                        "02:45",
                        180,
                        180,
                        "SCHEDULED"
                );

                Flight f2 = new Flight(
                        null,
                        "AI102",
                        "Air India",
                        "DELHI",
                        "MUMBAI",
                        flightDate,
                        LocalTime.of(10, 30),
                        flightDate,
                        LocalTime.of(12, 45),
                        "02:15",
                        180,
                        180,
                        "SCHEDULED"
                );

                Flight f3 = new Flight(
                        null,
                        "AI103",
                        "Air India",
                        "MUMBAI",
                        "BANGALORE",
                        flightDate,
                        LocalTime.of(14, 0),
                        flightDate,
                        LocalTime.of(15, 50),
                        "01:50",
                        180,
                        180,
                        "SCHEDULED"
                );

                Flight f4 = new Flight(
                        null,
                        "6E201",
                        "IndiGo",
                        "CHENNAI",
                        "MUMBAI",
                        flightDate,
                        LocalTime.of(7, 15),
                        flightDate,
                        LocalTime.of(9, 15),
                        "02:00",
                        180,
                        180,
                        "SCHEDULED"
                );

                Flight f5 = new Flight(
                        null,
                        "6E202",
                        "IndiGo",
                        "BANGALORE",
                        "DELHI",
                        flightDate,
                        LocalTime.of(16, 45),
                        flightDate,
                        LocalTime.of(19, 30),
                        "02:45",
                        180,
                        180,
                        "SCHEDULED"
                );

                Flight f6 = new Flight(
                        null,
                        "6E203",
                        "IndiGo",
                        "HYDERABAD",
                        "CHENNAI",
                        flightDate,
                        LocalTime.of(18, 0),
                        flightDate,
                        LocalTime.of(19, 15),
                        "01:15",
                        180,
                        180,
                        "SCHEDULED"
                );

                flightRepository.saveAll(Arrays.asList(f1, f2, f3, f4, f5, f6));
                logger.info("Successfully seeded 6 sample flights.");
            }
        };
    }
}
