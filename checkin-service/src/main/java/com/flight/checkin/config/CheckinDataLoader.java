package com.flight.checkin.config;

import com.flight.checkin.entity.CheckIn;
import com.flight.checkin.repository.CheckInRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class CheckinDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(CheckinDataLoader.class);

    @Bean
    public CommandLineRunner initCheckinData(CheckInRepository checkInRepository) {
        return args -> {
            try {
                if (!checkInRepository.existsById(1L) && !checkInRepository.findByCheckInReference("CHK-1001").isPresent()) {
                    logger.info("Seeding initial Check-in record with ID 1...");
                    CheckIn checkIn = new CheckIn();
                    checkIn.setId(1L);
                    checkIn.setCheckInReference("CHK-1001");
                    checkIn.setBookingId("BK1001");
                    checkIn.setPassengerId("P1001");
                    checkIn.setFlightId("FL1001");
                    checkIn.setSeatNumber("12A");
                    checkIn.setBaggageCount(1);
                    checkIn.setBaggageWeight(18.5);
                    checkIn.setStatus("CHECKED_IN");
                    checkIn.setCheckedInAt(LocalDateTime.now());
                    checkIn.setBoardingPassId("BP-1001");
                    checkIn.setCustomerEmail("customer@example.com");

                    checkInRepository.save(checkIn);
                    logger.info("Successfully seeded Check-in with ID 1");
                } else {
                    
                    checkInRepository.findById(1L).ifPresent(c -> {
                        if ("CANCELLED".equalsIgnoreCase(c.getStatus())) {
                            c.setStatus("CHECKED_IN");
                            checkInRepository.save(c);
                            logger.info("Reset Check-in ID 1 status to CHECKED_IN");
                        }
                    });
                }
            } catch (Exception e) {
                logger.warn("Seeding check-in 1 skipped or failed: {}", e.getMessage());
            }

            try {
              
                if (!checkInRepository.existsById(99L) && !checkInRepository.findByCheckInReference("CHK-DELETE-99").isPresent()) {
                    CheckIn delCheckIn = new CheckIn();
                    delCheckIn.setId(99L);
                    delCheckIn.setCheckInReference("CHK-DELETE-99");
                    delCheckIn.setBookingId("BK9999");
                    delCheckIn.setPassengerId("P9999");
                    delCheckIn.setFlightId("FL1001");
                    delCheckIn.setSeatNumber("20D");
                    delCheckIn.setBaggageCount(1);
                    delCheckIn.setBaggageWeight(15.0);
                    delCheckIn.setStatus("CHECKED_IN");
                    delCheckIn.setCheckedInAt(LocalDateTime.now());
                    delCheckIn.setBoardingPassId("BP-9999");
                    delCheckIn.setCustomerEmail("delete_test@example.com");

                    checkInRepository.save(delCheckIn);
                    logger.info("Successfully seeded dedicated delete Check-in with ID 99");
                }
            } catch (Exception e) {
                logger.warn("Seeding check-in 99 skipped or failed: {}", e.getMessage());
            }
        };
    }
}
