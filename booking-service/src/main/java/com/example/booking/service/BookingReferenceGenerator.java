package com.example.booking.service;

import com.example.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class BookingReferenceGenerator {

    private static final String REF_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final SecureRandom random = new SecureRandom();
    private final BookingRepository bookingRepository;

    public String generateReference() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String reference;
        int attempts = 0;
        do {
            StringBuilder suffix = new StringBuilder(4);
            for (int i = 0; i < 4; i++) {
                int index = random.nextInt(REF_CHARS.length());
                suffix.append(REF_CHARS.charAt(index));
            }
            reference = "BK-" + datePart + "-" + suffix;
            attempts++;
            if (attempts > 50) {
                throw new IllegalStateException("Failed to generate unique booking reference after 50 attempts");
            }
        } while (bookingRepository.existsByBookingReference(reference));

        return reference;
    }
}
