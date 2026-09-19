package com.flight.fare.config;

import com.flight.fare.entity.Fare;
import com.flight.fare.repository.FareRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
public class FareDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(FareDataLoader.class);

    @Bean
    public CommandLineRunner loadSampleFares(FareRepository fareRepository) {
        return args -> {
            if (fareRepository.count() == 0) {
                logger.info("Seeding sample fares into flight_fare_db...");

                Fare fare1 = new Fare(null, 1L, new BigDecimal("4500.00"), new BigDecimal("500.00"), new BigDecimal("200.00"), new BigDecimal("4800.00"), "INR");
                Fare fare2 = new Fare(null, 2L, new BigDecimal("5200.00"), new BigDecimal("600.00"), new BigDecimal("300.00"), new BigDecimal("5500.00"), "INR");
                Fare fare3 = new Fare(null, 3L, new BigDecimal("3800.00"), new BigDecimal("450.00"), new BigDecimal("150.00"), new BigDecimal("4100.00"), "INR");
                Fare fare4 = new Fare(null, 4L, new BigDecimal("4200.00"), new BigDecimal("480.00"), new BigDecimal("250.00"), new BigDecimal("4430.00"), "INR");
                Fare fare5 = new Fare(null, 5L, new BigDecimal("6000.00"), new BigDecimal("700.00"), new BigDecimal("400.00"), new BigDecimal("6300.00"), "INR");
                Fare fare6 = new Fare(null, 6L, new BigDecimal("3500.00"), new BigDecimal("400.00"), new BigDecimal("100.00"), new BigDecimal("3800.00"), "INR");

                fareRepository.saveAll(Arrays.asList(fare1, fare2, fare3, fare4, fare5, fare6));
                logger.info("Successfully seeded 6 sample fares.");
            }

            // Dedicated fare allocated exclusively for DELETE endpoint
            if (fareRepository.findByFlightId(99L).isEmpty()) {
                Fare delFare = new Fare(null, 99L, new BigDecimal("4000.00"), new BigDecimal("400.00"), new BigDecimal("100.00"), new BigDecimal("4300.00"), "INR");
                fareRepository.save(delFare);
                logger.info("Ensured dedicated delete fare for flight 99 exists.");
            }
        };
    }
}
