package com.example.fare.controller;

import com.example.fare.dto.message.FareValidationResponse;
import com.example.fare.dto.request.CreateFareRequest;
import com.example.fare.dto.request.ValidateFareRequest;
import com.example.fare.dto.response.FareResponse;
import com.example.fare.entity.CabinClass;
import com.example.fare.entity.FareStatus;
import com.example.fare.entity.FareType;
import com.example.fare.exception.FareNotFoundException;
import com.example.fare.service.FareService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FareController.class)
class FareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FareService fareService;

    @Test
    @DisplayName("POST /api/fares - Should return 201 Created")
    void testCreateFare_Success() throws Exception {
        CreateFareRequest request = CreateFareRequest.builder()
                .flightId(101L)
                .fareCode("ECONOMY-FLEX")
                .fareType(FareType.FLEXIBLE)
                .cabinClass(CabinClass.ECONOMY)
                .basePrice(BigDecimal.valueOf(5000))
                .taxAmount(BigDecimal.valueOf(900))
                .currency("INR")
                .availableSeats(10)
                .validFrom(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(30))
                .build();

        FareResponse response = FareResponse.builder()
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
                .build();

        when(fareService.createFare(any(CreateFareRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/fares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(501))
                .andExpect(jsonPath("$.fareCode").value("ECONOMY-FLEX"))
                .andExpect(jsonPath("$.totalPrice").value(5900));
    }

    @Test
    @DisplayName("GET /api/fares/{fareId} - Should return 200 OK")
    void testGetFareById_Success() throws Exception {
        FareResponse response = FareResponse.builder()
                .id(501L)
                .flightId(101L)
                .fareCode("ECONOMY-FLEX")
                .totalPrice(BigDecimal.valueOf(5900))
                .build();

        when(fareService.getFareById(501L)).thenReturn(response);

        mockMvc.perform(get("/api/fares/501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(501))
                .andExpect(jsonPath("$.totalPrice").value(5900));
    }

    @Test
    @DisplayName("GET /api/fares/{fareId} - Should return 404 Not Found")
    void testGetFareById_NotFound() throws Exception {
        when(fareService.getFareById(999L)).thenThrow(new FareNotFoundException("Fare not found with id: 999"));

        mockMvc.perform(get("/api/fares/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("FARE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/fares/validate - Should return 200 OK with pricing breakdown")
    void testValidateFare_Success() throws Exception {
        ValidateFareRequest request = ValidateFareRequest.builder()
                .flightId(101L)
                .fareId(501L)
                .requiredSeats(2)
                .build();

        FareValidationResponse response = FareValidationResponse.builder()
                .requestId("REQ-123456")
                .valid(true)
                .flightId(101L)
                .fareId(501L)
                .fareCode("ECONOMY-FLEX")
                .basePrice(BigDecimal.valueOf(10000))
                .taxAmount(BigDecimal.valueOf(1800))
                .totalPrice(BigDecimal.valueOf(11800))
                .currency("INR")
                .availableSeats(10)
                .message("Fare validated successfully")
                .build();

        when(fareService.validateFare(eq(101L), eq(501L), eq(2), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/fares/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.totalPrice").value(11800))
                .andExpect(jsonPath("$.currency").value("INR"));
    }
}
