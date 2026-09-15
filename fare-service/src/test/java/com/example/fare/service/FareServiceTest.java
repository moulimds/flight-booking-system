package com.example.fare.service;

import com.example.fare.dto.message.FareValidationResponse;
import com.example.fare.dto.request.CreateFareRequest;
import com.example.fare.dto.request.UpdateFareRequest;
import com.example.fare.dto.response.FareResponse;
import com.example.fare.entity.CabinClass;
import com.example.fare.entity.Fare;
import com.example.fare.entity.FareRule;
import com.example.fare.entity.FareStatus;
import com.example.fare.entity.FareType;
import com.example.fare.exception.FareNotFoundException;
import com.example.fare.mapper.FareMapper;
import com.example.fare.messaging.FareMessagePublisher;
import com.example.fare.repository.FareRepository;
import com.example.fare.service.impl.FareServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FareServiceTest {

    @Mock
    private FareRepository fareRepository;

    @Mock
    private FareMessagePublisher fareMessagePublisher;

    @Spy
    private FareMapper fareMapper = new FareMapper();

    @InjectMocks
    private FareServiceImpl fareService;

    private Fare testFare;
    private FareRule testRule;

    @BeforeEach
    void setUp() {
        testRule = FareRule.builder()
                .id(1L)
                .fareId(501L)
                .baggageAllowance("15 kg Check-in, 7 kg Cabin")
                .cancellationAllowed(true)
                .cancellationFee(BigDecimal.valueOf(1500))
                .dateChangeAllowed(true)
                .dateChangeFee(BigDecimal.valueOf(1000))
                .mealSelectionAllowed(true)
                .seatSelectionAllowed(true)
                .refundAllowed(true)
                .build();

        testFare = Fare.builder()
                .id(501L)
                .flightId(101L)
                .fareCode("ECONOMY-FLEX")
                .fareType(FareType.FLEXIBLE)
                .cabinClass(CabinClass.ECONOMY)
                .basePrice(BigDecimal.valueOf(5000))
                .taxAmount(BigDecimal.valueOf(900))
                .totalPrice(BigDecimal.valueOf(5900))
                .currency("INR")
                .availableSeats(10)
                .status(FareStatus.ACTIVE)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .fareRule(testRule)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0L)
                .build();
    }

    @Test
    @DisplayName("Should create fare successfully")
    void testCreateFare() {
        CreateFareRequest request = CreateFareRequest.builder()
                .flightId(101L)
                .fareCode("ECONOMY-FLEX")
                .fareType(FareType.FLEXIBLE)
                .cabinClass(CabinClass.ECONOMY)
                .basePrice(BigDecimal.valueOf(5000))
                .taxAmount(BigDecimal.valueOf(900))
                .currency("INR")
                .availableSeats(10)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .baggageAllowance("15 kg")
                .cancellationAllowed(true)
                .dateChangeAllowed(true)
                .mealSelectionAllowed(true)
                .seatSelectionAllowed(true)
                .refundAllowed(true)
                .build();

        when(fareRepository.save(any(Fare.class))).thenReturn(testFare);

        FareResponse response = fareService.createFare(request);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals("ECONOMY-FLEX", response.getFareCode());
        verify(fareMessagePublisher).publishFareCreated(any(FareResponse.class));
    }

    @Test
    @DisplayName("Should get fare by ID successfully")
    void testGetFareById() {
        when(fareRepository.findById(501L)).thenReturn(Optional.of(testFare));

        FareResponse response = fareService.getFareById(501L);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals(BigDecimal.valueOf(5900), response.getTotalPrice());
    }

    @Test
    @DisplayName("Should throw FareNotFoundException when fare ID does not exist")
    void testGetFareById_NotFound() {
        when(fareRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(FareNotFoundException.class, () -> fareService.getFareById(999L));
    }

    @Test
    @DisplayName("Should validate active fare with sufficient seats successfully")
    void testValidateFare_Success() {
        when(fareRepository.findByIdAndFlightId(501L, 101L)).thenReturn(Optional.of(testFare));

        FareValidationResponse response = fareService.validateFare(101L, 501L, 2, "REQ-123");

        assertNotNull(response);
        assertTrue(response.getValid());
        assertEquals("REQ-123", response.getRequestId());
        assertEquals(BigDecimal.valueOf(10000), response.getBasePrice()); // 5000 * 2
        assertEquals(BigDecimal.valueOf(1800), response.getTaxAmount());   // 900 * 2
        assertEquals(BigDecimal.valueOf(11800), response.getTotalPrice()); // 5900 * 2
    }

    @Test
    @DisplayName("Should fail validation if available seats are insufficient")
    void testValidateFare_InsufficientSeats() {
        testFare.setAvailableSeats(1);
        when(fareRepository.findByIdAndFlightId(501L, 101L)).thenReturn(Optional.of(testFare));

        FareValidationResponse response = fareService.validateFare(101L, 501L, 3, "REQ-123");

        assertNotNull(response);
        assertFalse(response.getValid());
        assertTrue(response.getMessage().contains("Insufficient seats"));
    }

    @Test
    @DisplayName("Should fail validation if fare is expired")
    void testValidateFare_Expired() {
        testFare.setValidUntil(LocalDateTime.now().minusDays(1));
        when(fareRepository.findByIdAndFlightId(501L, 101L)).thenReturn(Optional.of(testFare));

        FareValidationResponse response = fareService.validateFare(101L, 501L, 1, "REQ-123");

        assertNotNull(response);
        assertFalse(response.getValid());
        assertTrue(response.getMessage().contains("expired"));
    }

    @Test
    @DisplayName("Should fail validation if fare is INACTIVE")
    void testValidateFare_Inactive() {
        testFare.setStatus(FareStatus.INACTIVE);
        when(fareRepository.findByIdAndFlightId(501L, 101L)).thenReturn(Optional.of(testFare));

        FareValidationResponse response = fareService.validateFare(101L, 501L, 1, "REQ-123");

        assertNotNull(response);
        assertFalse(response.getValid());
        assertTrue(response.getMessage().contains("INACTIVE"));
    }
}
