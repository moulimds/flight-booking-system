package com.example.booking.controller;

import com.example.booking.dto.request.CancelBookingRequest;
import com.example.booking.dto.request.CreateBookingRequest;
import com.example.booking.dto.request.PassengerRequest;
import com.example.booking.dto.response.BookingCancellationResponse;
import com.example.booking.dto.response.BookingResponse;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.Gender;
import com.example.booking.entity.PassengerType;
import com.example.booking.exception.BookingNotFoundException;
import com.example.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("POST /api/bookings - Should create booking and return 201 Created")
    void testCreateBooking_Success() throws Exception {
        PassengerRequest pReq = PassengerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1995, 5, 10))
                .gender(Gender.MALE)
                .passengerType(PassengerType.ADULT)
                .build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(1001L)
                .flightId(101L)
                .fareId(501L)
                .passengers(List.of(pReq))
                .build();

        BookingResponse response = BookingResponse.builder()
                .id(10001L)
                .bookingReference("BK-20260913-A7K2")
                .pnr("A7K92P")
                .userId(1001L)
                .flightId(101L)
                .fareId(501L)
                .totalAmount(BigDecimal.valueOf(5900))
                .currency("INR")
                .bookingStatus(BookingStatus.PAYMENT_PENDING)
                .build();

        when(bookingService.createBooking(any(CreateBookingRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "idemp-12345")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10001))
                .andExpect(jsonPath("$.pnr").value("A7K92P"))
                .andExpect(jsonPath("$.bookingReference").value("BK-20260913-A7K2"))
                .andExpect(jsonPath("$.totalAmount").value(5900));
    }

    @Test
    @DisplayName("GET /api/bookings/pnr/{pnr} - Should return booking by PNR")
    void testGetBookingByPnr_Success() throws Exception {
        BookingResponse response = BookingResponse.builder()
                .id(10001L)
                .pnr("A7K92P")
                .userId(1001L)
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.getBookingByPnr("A7K92P")).thenReturn(response);

        mockMvc.perform(get("/api/bookings/pnr/A7K92P"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pnr").value("A7K92P"))
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/bookings/{bookingId} - Should return 404 if booking does not exist")
    void testGetBookingById_NotFound() throws Exception {
        when(bookingService.getBookingById(99999L)).thenThrow(new BookingNotFoundException("Booking not found with id: 99999"));

        mockMvc.perform(get("/api/bookings/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("BOOKING_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/bookings/{bookingId}/cancel - Should cancel booking")
    void testCancelBooking_Success() throws Exception {
        CancelBookingRequest cancelReq = CancelBookingRequest.builder().reason("Travel plan changed").build();
        BookingCancellationResponse cancelRes = BookingCancellationResponse.builder()
                .bookingReference("BK-20260913-A7K2")
                .pnr("A7K92P")
                .originalAmount(BigDecimal.valueOf(5900))
                .cancellationFee(BigDecimal.valueOf(1500))
                .refundAmount(BigDecimal.valueOf(4400))
                .currency("INR")
                .status(BookingStatus.CANCELLED)
                .reason("Travel plan changed")
                .build();

        when(bookingService.cancelBooking(eq(10001L), any(CancelBookingRequest.class))).thenReturn(cancelRes);

        mockMvc.perform(post("/api/bookings/10001/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationFee").value(1500))
                .andExpect(jsonPath("$.refundAmount").value(4400));
    }

    @Test
    @DisplayName("POST /api/bookings/hold - Should create temporary seat hold")
    void testHoldBooking_Success() throws Exception {
        com.example.booking.dto.request.HoldBookingRequest request = com.example.booking.dto.request.HoldBookingRequest.builder()
                .userId(1001L)
                .flightId(101L)
                .fareId(501L)
                .holdDurationMinutes(15)
                .passengers(List.of(
                        PassengerRequest.builder()
                                .firstName("Jane")
                                .lastName("Doe")
                                .dateOfBirth(LocalDate.of(1995, 5, 10))
                                .gender(Gender.FEMALE)
                                .passengerType(PassengerType.ADULT)
                                .seatNumber("3A")
                                .build()
                ))
                .build();

        BookingResponse response = BookingResponse.builder()
                .id(20002L)
                .pnr("HLD999")
                .bookingReference("BK-HOLD-123")
                .bookingStatus(BookingStatus.HELD)
                .totalAmount(BigDecimal.valueOf(5900))
                .build();

        when(bookingService.holdBooking(any())).thenReturn(response);

        mockMvc.perform(post("/api/bookings/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingStatus").value("HELD"))
                .andExpect(jsonPath("$.pnr").value("HLD999"));
    }

    @Test
    @DisplayName("GET /api/bookings/seats/flight/{flightId} - Should return seat list")
    void testGetFlightSeats_Success() throws Exception {
        com.example.booking.dto.response.FlightSeatResponse s1 = com.example.booking.dto.response.FlightSeatResponse.builder()
                .id(1L)
                .flightId(101L)
                .seatNumber("1A")
                .cabinClass(com.example.booking.entity.CabinClass.BUSINESS)
                .seatStatus(com.example.booking.entity.SeatStatus.AVAILABLE)
                .isAvailable(true)
                .build();

        when(bookingService.getFlightSeats(101L)).thenReturn(List.of(s1));

        mockMvc.perform(get("/api/bookings/seats/flight/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatNumber").value("1A"))
                .andExpect(jsonPath("$[0].seatStatus").value("AVAILABLE"));
    }
}
