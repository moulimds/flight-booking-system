package com.example.booking.service;

import com.example.booking.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PnrGeneratorTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PnrGenerator pnrGenerator;

    @Test
    @DisplayName("Should generate a 6-character uppercase alphanumeric PNR")
    void testGeneratePnr() {
        when(bookingRepository.existsByPnr(anyString())).thenReturn(false);

        String pnr = pnrGenerator.generatePnr();

        assertNotNull(pnr);
        assertEquals(6, pnr.length());
        assertTrue(pnr.matches("^[A-Z0-9]{6}$"));
    }
}
