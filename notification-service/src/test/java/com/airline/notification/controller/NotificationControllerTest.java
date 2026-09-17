package com.airline.notification.controller;

import com.airline.notification.dto.*;
import com.airline.notification.entity.NotificationChannel;
import com.airline.notification.entity.NotificationStatus;
import com.airline.notification.entity.NotificationType;
import com.airline.notification.service.NotificationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    private NotificationResponse sampleResponse() {
        NotificationResponse dto = new NotificationResponse();
        dto.setId(1L);
        dto.setUserId("USER-1002");
        dto.setRecipient("jane.smith@example.com");
        dto.setChannel(NotificationChannel.EMAIL);
        dto.setType(NotificationType.BOOKING_CONFIRMATION);
        dto.setSubject("Flight Booking Confirmed");
        dto.setContent("Booking confirmed for FL-101");
        dto.setStatus(NotificationStatus.SENT);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    @Test
    @DisplayName("POST /api/v1/notifications/send-direct - Success")
    void sendDirectNotification_Endpoint_Success() throws Exception {
        DirectNotificationRequest req = new DirectNotificationRequest();
        req.setUserId("USER-1002");
        req.setRecipient("jane.smith@example.com");
        req.setChannel(NotificationChannel.EMAIL);
        req.setType(NotificationType.BOOKING_CONFIRMATION);
        req.setContent("Test email content");

        when(notificationService.sendDirectNotification(any(DirectNotificationRequest.class)))
                .thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/notifications/send-direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification processed successfully"))
                .andExpect(jsonPath("$.data.recipient").value("jane.smith@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/logs/{id} - Success")
    void getLogById_Endpoint_Success() throws Exception {
        when(notificationService.getLogById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/notifications/logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/logs/user/{userId} - Success")
    void getLogsByUserId_Endpoint_Success() throws Exception {
        when(notificationService.getLogsByUserId("USER-1002")).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/notifications/logs/user/USER-1002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value("USER-1002"));
    }

    @Test
    @DisplayName("POST /api/v1/notifications/simulate-event/booking - Success")
    void simulateBookingEvent_Endpoint_Success() throws Exception {
        BookingEventDTO event = new BookingEventDTO();
        event.setBookingId("BKG-77123");
        event.setUserId("USER-1003");
        event.setCustomerEmail("alice.johnson@example.com");
        event.setFlightNumber("FL-101");
        event.setBookingReference("BK-2026-9912");
        event.setTotalPrice(new BigDecimal("750.00"));
        event.setEventType("CONFIRMED");

        doNothing().when(notificationService).processBookingEvent(any(BookingEventDTO.class));

        mockMvc.perform(post("/api/v1/notifications/simulate-event/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Booking event simulated and notification dispatched"));
    }

    @Test
    @DisplayName("POST /api/v1/notifications/simulate-event/payment - Success")
    void simulatePaymentEvent_Endpoint_Success() throws Exception {
        PaymentEventDTO event = new PaymentEventDTO();
        event.setPaymentId("PAY-882910");
        event.setBookingReference("BK-2026-9912");
        event.setUserId("USER-1003");
        event.setCustomerEmail("alice.johnson@example.com");
        event.setAmount(new BigDecimal("750.00"));
        event.setCurrency("USD");
        event.setPaymentStatus("SUCCESS");

        doNothing().when(notificationService).processPaymentEvent(any(PaymentEventDTO.class));

        mockMvc.perform(post("/api/v1/notifications/simulate-event/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment event simulated and notification dispatched"));
    }

    @Test
    @DisplayName("POST /api/v1/notifications/simulate-event/flight-alert - Success")
    void simulateFlightAlertEvent_Endpoint_Success() throws Exception {
        FlightAlertEventDTO event = new FlightAlertEventDTO();
        event.setFlightNumber("FL-101");
        event.setOrigin("JFK");
        event.setDestination("LHR");
        event.setAlertType("DELAYED");

        doNothing().when(notificationService).processFlightAlertEvent(any(FlightAlertEventDTO.class));

        mockMvc.perform(post("/api/v1/notifications/simulate-event/flight-alert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Flight alert event simulated and notifications broadcasted"));
    }

    @Test
    @DisplayName("POST /api/v1/notifications/simulate-event/auth-verification - Success")
    void simulateAuthVerificationEvent_Endpoint_Success() throws Exception {
        AuthVerificationDTO event = new AuthVerificationDTO();
        event.setUserId("USER-1004");
        event.setEmail("david.miller@example.com");
        event.setOtpCode("748921");
        event.setVerificationType("LOGIN_2FA");

        doNothing().when(notificationService).processAuthVerificationEvent(any(AuthVerificationDTO.class));

        mockMvc.perform(post("/api/v1/notifications/simulate-event/auth-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Auth verification OTP dispatched"));
    }
}
