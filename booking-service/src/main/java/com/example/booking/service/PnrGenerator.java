package com.example.booking.service;

import com.example.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class PnrGenerator {

    private static final String PNR_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int PNR_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();
    private final BookingRepository bookingRepository;

    public String generatePnr() {
        String pnr;
        int attempts = 0;
        do {
            StringBuilder sb = new StringBuilder(PNR_LENGTH);
            for (int i = 0; i < PNR_LENGTH; i++) {
                int index = random.nextInt(PNR_CHARS.length());
                sb.append(PNR_CHARS.charAt(index));
            }
            pnr = sb.toString();
            attempts++;
            if (attempts > 50) {
                throw new IllegalStateException("Failed to generate unique PNR after 50 attempts");
            }
        } while (bookingRepository.existsByPnr(pnr));

        return pnr;
    }
}
