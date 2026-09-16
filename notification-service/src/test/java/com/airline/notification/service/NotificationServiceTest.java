package com.airline.notification.service;

import com.airline.notification.dto.*;
import com.airline.notification.entity.*;
import com.airline.notification.exception.ResourceNotFoundException;
import com.airline.notification.repository.NotificationLogRepository;
import com.airline.notification.repository.NotificationTemplateRepository;
import com.airline.notification.service.impl.NotificationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository logRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    private EmailSenderService emailSenderService;
    private SmsSenderService smsSenderService;
    private ObjectMapper objectMapper;

    private NotificationServiceImpl notificationService;

    private NotificationTemplate emailTemplate;

    @BeforeEach
    void setUp() {
        emailSenderService = new EmailSenderService();
        smsSenderService = new SmsSenderService();
        objectMapper = new ObjectMapper();

        notificationService = new NotificationServiceImpl(
                logRepository,
                templateRepository,
                emailSenderService,
                smsSenderService,
                objectMapper
        );

        emailTemplate = new NotificationTemplate(
                "BOOKING_CONFIRMATION_EMAIL",
                "Flight Confirmed: {{bookingReference}}",
                "Hello {{name}}, your booking {{bookingReference}} for flight {{flightNumber}} is confirmed.",
                NotificationChannel.EMAIL,
                true
        );
    }

    @Test
    @DisplayName("Send Direct Notification - Email with template substitution")
    void sendDirectNotification_EmailTemplate_Success() {
        DirectNotificationRequest req = new DirectNotificationRequest();
        req.setUserId("USER-1002");
        req.setRecipient("jane.smith@example.com");
        req.setChannel(NotificationChannel.EMAIL);
        req.setType(NotificationType.BOOKING_CONFIRMATION);
        req.setTemplateCode("BOOKING_CONFIRMATION_EMAIL");
        req.setTemplateVariables(Map.of(
                "name", "Jane Smith",
                "bookingReference", "BK-2026-8841",
                "flightNumber", "FL-101"
        ));

        when(templateRepository.findByTemplateCode("BOOKING_CONFIRMATION_EMAIL"))
                .thenReturn(Optional.of(emailTemplate));
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        NotificationResponse response = notificationService.sendDirectNotification(req);

        assertNotNull(response);
        assertEquals(NotificationStatus.SENT, response.getStatus());
        assertEquals("jane.smith@example.com", response.getRecipient());
        assertEquals("Flight Confirmed: BK-2026-8841", response.getSubject());
        assertTrue(response.getContent().contains("Jane Smith"));
        verify(logRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Send Direct Notification - SMS with direct content")
    void sendDirectNotification_SmsDirect_Success() {
        DirectNotificationRequest req = new DirectNotificationRequest();
        req.setUserId("USER-1002");
        req.setRecipient("+15552345678");
        req.setChannel(NotificationChannel.SMS);
        req.setType(NotificationType.DIRECT_MESSAGE);
        req.setContent("SkyWings Alert: Gate change to B14.");

        when(logRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(2L);
            return log;
        });

        NotificationResponse response = notificationService.sendDirectNotification(req);

        assertNotNull(response);
        assertEquals(NotificationStatus.SENT, response.getStatus());
        assertEquals("+15552345678", response.getRecipient());
        verify(logRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Send Direct Notification - Invalid Email captures FAILED status in audit log")
    void sendDirectNotification_InvalidEmail_LogsFailed() {
        DirectNotificationRequest req = new DirectNotificationRequest();
        req.setUserId("USER-1002");
        req.setRecipient("invalid-email-address");
        req.setChannel(NotificationChannel.EMAIL);
        req.setType(NotificationType.DIRECT_MESSAGE);
        req.setContent("Test direct content");

        when(logRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(3L);
            return log;
        });

        NotificationResponse response = notificationService.sendDirectNotification(req);

        assertNotNull(response);
        assertEquals(NotificationStatus.FAILED, response.getStatus());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    @DisplayName("Get Notification Log by ID - Success")
    void getLogById_Success() {
        NotificationLog logEntry = new NotificationLog(
                "USER-1001", "john.doe@example.com", NotificationChannel.EMAIL,
                NotificationType.BOOKING_CONFIRMATION, "Flight Confirmed",
                "Your flight is confirmed.", NotificationStatus.SENT, null, null
        );
        logEntry.setId(1L);

        when(logRepository.findById(1L)).thenReturn(Optional.of(logEntry));

        NotificationResponse response = notificationService.getLogById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("USER-1001", response.getUserId());
    }

    @Test
    @DisplayName("Get Notification Log by ID - Not Found Throws Exception")
    void getLogById_NotFound_ThrowsException() {
        when(logRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.getLogById(999L));
    }

    @Test
    @DisplayName("Get Logs By User ID - Returns ordered list")
    void getLogsByUserId_Success() {
        NotificationLog logEntry = new NotificationLog(
                "USER-1001", "john.doe@example.com", NotificationChannel.EMAIL,
                NotificationType.BOOKING_CONFIRMATION, "Subject", "Content",
                NotificationStatus.SENT, null, null
        );
        logEntry.setId(1L);

        when(logRepository.findByUserIdOrderByCreatedAtDesc("USER-1001")).thenReturn(List.of(logEntry));

        List<NotificationResponse> responses = notificationService.getLogsByUserId("USER-1001");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("USER-1001", responses.get(0).getUserId());
    }

    @Test
    @DisplayName("Process Booking Event - Confirmed event dispatches email and SMS")
    void processBookingEvent_Confirmed_Success() {
        BookingEventDTO event = new BookingEventDTO();
        event.setBookingId("BKG-77123");
        event.setBookingReference("BK-2026-9912");
        event.setUserId("USER-1003");
        event.setCustomerName("Alice Johnson");
        event.setCustomerEmail("alice.johnson@example.com");
        event.setCustomerPhone("+15559876543");
        event.setFlightNumber("FL-101");
        event.setSeatNumbers(List.of("2A", "2D"));
        event.setTotalPrice(new BigDecimal("750.00"));
        event.setDepartureTime("2026-09-16 10:00 AM");
        event.setEventType("CONFIRMED");

        when(templateRepository.findByTemplateCode("BOOKING_CONFIRMATION_EMAIL"))
                .thenReturn(Optional.of(emailTemplate));
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.processBookingEvent(event);

        verify(logRepository, times(2)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Process Payment Event - Dispatches payment receipt email")
    void processPaymentEvent_Success() {
        PaymentEventDTO event = new PaymentEventDTO();
        event.setPaymentId("PAY-882910");
        event.setBookingReference("BK-2026-9912");
        event.setUserId("USER-1003");
        event.setCustomerEmail("alice.johnson@example.com");
        event.setAmount(new BigDecimal("750.00"));
        event.setCurrency("USD");
        event.setPaymentStatus("SUCCESS");

        NotificationTemplate paymentTemplate = new NotificationTemplate(
                "PAYMENT_RECEIPT_EMAIL", "Receipt: {{bookingReference}}",
                "Paid {{currency}} {{amount}}", NotificationChannel.EMAIL, true
        );

        when(templateRepository.findByTemplateCode("PAYMENT_RECEIPT_EMAIL"))
                .thenReturn(Optional.of(paymentTemplate));
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.processPaymentEvent(event);

        verify(logRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Process Flight Alert Event - Broadcasts to emails and SMS")
    void processFlightAlertEvent_Success() {
        FlightAlertEventDTO event = new FlightAlertEventDTO();
        event.setFlightNumber("FL-101");
        event.setOrigin("JFK");
        event.setDestination("LHR");
        event.setAlertType("DELAYED");
        event.setDelayReason("Weather");
        event.setAffectedUserEmails(List.of("alice@example.com", "bob@example.com"));
        event.setAffectedUserPhones(List.of("+15551112222"));

        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.processFlightAlertEvent(event);

        verify(logRepository, times(3)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Process Auth Verification Event - Sends OTP via Email and SMS")
    void processAuthVerificationEvent_Success() {
        AuthVerificationDTO event = new AuthVerificationDTO();
        event.setUserId("USER-1004");
        event.setEmail("david@example.com");
        event.setPhoneNumber("+15553334444");
        event.setUserName("David Miller");
        event.setOtpCode("748921");
        event.setVerificationType("LOGIN_2FA");
        event.setExpiresInMinutes(5);

        NotificationTemplate otpEmail = new NotificationTemplate("AUTH_OTP_EMAIL", "OTP: {{otpCode}}", "Code is {{otpCode}}", NotificationChannel.EMAIL, true);
        NotificationTemplate otpSms = new NotificationTemplate("AUTH_OTP_SMS", null, "OTP: {{otpCode}}", NotificationChannel.SMS, true);

        when(templateRepository.findByTemplateCode("AUTH_OTP_EMAIL")).thenReturn(Optional.of(otpEmail));
        when(templateRepository.findByTemplateCode("AUTH_OTP_SMS")).thenReturn(Optional.of(otpSms));
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.processAuthVerificationEvent(event);

        verify(logRepository, times(2)).save(any(NotificationLog.class));
    }
}
