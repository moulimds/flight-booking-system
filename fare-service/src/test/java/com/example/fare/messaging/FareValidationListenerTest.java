package com.example.fare.messaging;

import com.example.fare.dto.message.FareValidationRequest;
import com.example.fare.dto.message.FareValidationResponse;
import com.example.fare.service.FareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FareValidationListenerTest {

    @Mock
    private FareService fareService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private FareValidationListener listener;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listener, "exchange", "flight.booking.exchange");
        ReflectionTestUtils.setField(listener, "fareResponseRoutingKey", "fare.validation.response");
    }

    @Test
    @DisplayName("Should consume validation request, invoke service, and reply with correlated message")
    void testHandleFareValidationRequest_Success() {
        FareValidationRequest request = FareValidationRequest.builder()
                .requestId("REQ-999")
                .flightId(101L)
                .fareId(501L)
                .requiredSeats(2)
                .build();

        FareValidationResponse expectedResponse = FareValidationResponse.builder()
                .requestId("REQ-999")
                .valid(true)
                .flightId(101L)
                .fareId(501L)
                .fareCode("ECONOMY-FLEX")
                .totalPrice(BigDecimal.valueOf(11800))
                .currency("INR")
                .build();

        when(fareService.validateFare(101L, 501L, 2, "REQ-999")).thenReturn(expectedResponse);

        listener.handleFareValidationRequest(request, "REQ-999", "fare.validation.response");

        verify(fareService).validateFare(101L, 501L, 2, "REQ-999");
        verify(rabbitTemplate).convertAndSend(
                eq("flight.booking.exchange"),
                eq("fare.validation.response"),
                eq(expectedResponse),
                any(MessagePostProcessor.class)
        );
    }
}
