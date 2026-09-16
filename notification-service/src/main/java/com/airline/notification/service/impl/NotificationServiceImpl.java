package com.airline.notification.service.impl;

import com.airline.notification.dto.*;
import com.airline.notification.entity.*;
import com.airline.notification.exception.NotificationDeliveryException;
import com.airline.notification.exception.ResourceNotFoundException;
import com.airline.notification.repository.NotificationLogRepository;
import com.airline.notification.repository.NotificationTemplateRepository;
import com.airline.notification.service.EmailSenderService;
import com.airline.notification.service.NotificationService;
import com.airline.notification.service.SmsSenderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationLogRepository logRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailSenderService emailSenderService;
    private final SmsSenderService smsSenderService;
    private final ObjectMapper objectMapper;

    public NotificationServiceImpl(NotificationLogRepository logRepository,
                                   NotificationTemplateRepository templateRepository,
                                   EmailSenderService emailSenderService,
                                   SmsSenderService smsSenderService,
                                   ObjectMapper objectMapper) {
        this.logRepository = logRepository;
        this.templateRepository = templateRepository;
        this.emailSenderService = emailSenderService;
        this.smsSenderService = smsSenderService;
        this.objectMapper = objectMapper;
    }

    @Override
    public NotificationResponse sendDirectNotification(DirectNotificationRequest request) {
        log.info("Sending direct notification to recipient: {} via channel: {}", request.getRecipient(), request.getChannel());

        String subject = request.getSubject();
        String content = request.getContent();

        // If template code is provided, render subject and content from template
        if (request.getTemplateCode() != null && !request.getTemplateCode().isBlank()) {
            NotificationTemplate template = templateRepository.findByTemplateCode(request.getTemplateCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Notification template not found: " + request.getTemplateCode()));

            Map<String, String> variables = request.getTemplateVariables() != null ? request.getTemplateVariables() : Collections.emptyMap();
            subject = substituteVariables(template.getSubjectTemplate(), variables);
            content = substituteVariables(template.getBodyTemplate(), variables);
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Notification content or valid templateCode with variables must be provided.");
        }

        NotificationStatus status = NotificationStatus.SENT;
        String errorMessage = null;

        try {
            if (request.getChannel() == NotificationChannel.EMAIL) {
                emailSenderService.sendEmail(request.getRecipient(), subject != null ? subject : "Airline Notification", content);
            } else if (request.getChannel() == NotificationChannel.SMS) {
                smsSenderService.sendSms(request.getRecipient(), content);
            }
        } catch (Exception ex) {
            log.error("Failed to deliver notification to {}: {}", request.getRecipient(), ex.getMessage());
            status = NotificationStatus.FAILED;
            errorMessage = ex.getMessage();
        }

        String metadataJson = null;
        if (request.getTemplateVariables() != null) {
            try {
                metadataJson = objectMapper.writeValueAsString(request.getTemplateVariables());
            } catch (JsonProcessingException ignored) {}
        }

        NotificationLog logEntry = new NotificationLog(
                request.getUserId(),
                request.getRecipient(),
                request.getChannel(),
                request.getType(),
                subject,
                content,
                status,
                errorMessage,
                metadataJson
        );

        NotificationLog saved = logRepository.save(logEntry);
        return NotificationResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getLogById(Long id) {
        NotificationLog notificationLog = logRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification log not found with ID: " + id));
        return NotificationResponse.from(notificationLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getLogsByUserId(String userId) {
        return logRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void processBookingEvent(BookingEventDTO event) {
        log.info("Processing booking event: ref={}, user={}, type={}",
                event.getBookingReference(), event.getUserId(), event.getEventType());

        Map<String, String> vars = new HashMap<>();
        vars.put("name", event.getCustomerName() != null ? event.getCustomerName() : "Valued Customer");
        vars.put("bookingReference", event.getBookingReference());
        vars.put("flightNumber", event.getFlightNumber());
        vars.put("seatNumbers", event.getSeatNumbers() != null ? String.join(", ", event.getSeatNumbers()) : "N/A");
        vars.put("totalPrice", event.getTotalPrice() != null ? event.getTotalPrice().toString() : "0.00");
        vars.put("departureTime", event.getDepartureTime() != null ? event.getDepartureTime() : "As scheduled");

        boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(event.getEventType()) || event.getEventType() == null;
        String templateCode = isConfirmed ? "BOOKING_CONFIRMATION_EMAIL" : "BOOKING_CANCELLATION_EMAIL";

        DirectNotificationRequest req = new DirectNotificationRequest();
        req.setUserId(event.getUserId());
        req.setRecipient(event.getCustomerEmail() != null ? event.getCustomerEmail() : "guest@airline.com");
        req.setChannel(NotificationChannel.EMAIL);
        req.setType(isConfirmed ? NotificationType.BOOKING_CONFIRMATION : NotificationType.BOOKING_CANCELLATION);
        req.setTemplateCode(templateCode);
        req.setTemplateVariables(vars);

        sendDirectNotification(req);

        // Also send brief SMS confirmation if phone number is provided
        if (event.getCustomerPhone() != null && !event.getCustomerPhone().isBlank()) {
            DirectNotificationRequest smsReq = new DirectNotificationRequest();
            smsReq.setUserId(event.getUserId());
            smsReq.setRecipient(event.getCustomerPhone());
            smsReq.setChannel(NotificationChannel.SMS);
            smsReq.setType(isConfirmed ? NotificationType.BOOKING_CONFIRMATION : NotificationType.BOOKING_CANCELLATION);
            smsReq.setContent(String.format("Airline Alert: Booking %s for Flight %s is %s. Seats: %s. Safe travels!",
                    event.getBookingReference(), event.getFlightNumber(),
                    isConfirmed ? "CONFIRMED" : "CANCELLED",
                    vars.get("seatNumbers")));
            sendDirectNotification(smsReq);
        }
    }

    @Override
    public void processPaymentEvent(PaymentEventDTO event) {
        log.info("Processing payment event: ref={}, amount={}, status={}",
                event.getBookingReference(), event.getAmount(), event.getPaymentStatus());

        Map<String, String> vars = new HashMap<>();
        vars.put("bookingReference", event.getBookingReference());
        vars.put("paymentId", event.getPaymentId() != null ? event.getPaymentId() : "PAY-" + UUID.randomUUID());
        vars.put("amount", event.getAmount() != null ? event.getAmount().toString() : "0.00");
        vars.put("currency", event.getCurrency() != null ? event.getCurrency() : "USD");
        vars.put("status", event.getPaymentStatus());
        vars.put("transactionTime", event.getTransactionTime() != null ? event.getTransactionTime() : LocalDateTime.now().toString());

        boolean isRefund = "REFUNDED".equalsIgnoreCase(event.getPaymentStatus());

        DirectNotificationRequest req = new DirectNotificationRequest();
        req.setUserId(event.getUserId());
        req.setRecipient(event.getCustomerEmail() != null ? event.getCustomerEmail() : "billing@airline.com");
        req.setChannel(NotificationChannel.EMAIL);
        req.setType(isRefund ? NotificationType.REFUND_ALERT : NotificationType.PAYMENT_ALERT);
        req.setTemplateCode("PAYMENT_RECEIPT_EMAIL");
        req.setTemplateVariables(vars);

        sendDirectNotification(req);
    }

    @Override
    public void processFlightAlertEvent(FlightAlertEventDTO event) {
        log.info("Processing flight alert event: flight={}, alertType={}, reason={}",
                event.getFlightNumber(), event.getAlertType(), event.getDelayReason());

        String message = String.format("URGENT AIRLINE ALERT: Flight %s (%s to %s) has an update. Status: %s. %s. New Dept: %s, Gate: %s.",
                event.getFlightNumber(),
                event.getOrigin() != null ? event.getOrigin() : "Origin",
                event.getDestination() != null ? event.getDestination() : "Destination",
                event.getAlertType(),
                event.getDelayReason() != null ? "Reason: " + event.getDelayReason() : "",
                event.getEstimatedDepartureTime() != null ? event.getEstimatedDepartureTime() : "TBD",
                event.getGateNumber() != null ? event.getGateNumber() : "TBD");

        if (event.getAffectedUserPhones() != null) {
            for (String phone : event.getAffectedUserPhones()) {
                DirectNotificationRequest smsReq = new DirectNotificationRequest();
                smsReq.setRecipient(phone);
                smsReq.setChannel(NotificationChannel.SMS);
                smsReq.setType(NotificationType.DELAY_ALERT);
                smsReq.setContent(message);
                sendDirectNotification(smsReq);
            }
        }

        if (event.getAffectedUserEmails() != null) {
            for (String email : event.getAffectedUserEmails()) {
                DirectNotificationRequest emailReq = new DirectNotificationRequest();
                emailReq.setRecipient(email);
                emailReq.setChannel(NotificationChannel.EMAIL);
                emailReq.setType(NotificationType.DELAY_ALERT);
                emailReq.setSubject("Flight Update Alert: " + event.getFlightNumber());
                emailReq.setContent(message);
                sendDirectNotification(emailReq);
            }
        }
    }

    @Override
    public void processAuthVerificationEvent(AuthVerificationDTO event) {
        log.info("Processing auth verification OTP for user: {}, type: {}", event.getUserId(), event.getVerificationType());

        Map<String, String> vars = new HashMap<>();
        vars.put("name", event.getUserName() != null ? event.getUserName() : "User");
        vars.put("otpCode", event.getOtpCode());
        vars.put("verificationType", event.getVerificationType());
        vars.put("expiresInMinutes", event.getExpiresInMinutes() != null ? event.getExpiresInMinutes().toString() : "10");

        if (event.getEmail() != null && !event.getEmail().isBlank()) {
            DirectNotificationRequest emailReq = new DirectNotificationRequest();
            emailReq.setUserId(event.getUserId());
            emailReq.setRecipient(event.getEmail());
            emailReq.setChannel(NotificationChannel.EMAIL);
            emailReq.setType(NotificationType.AUTH_VERIFICATION);
            emailReq.setTemplateCode("AUTH_OTP_EMAIL");
            emailReq.setTemplateVariables(vars);
            sendDirectNotification(emailReq);
        }

        if (event.getPhoneNumber() != null && !event.getPhoneNumber().isBlank()) {
            DirectNotificationRequest smsReq = new DirectNotificationRequest();
            smsReq.setUserId(event.getUserId());
            smsReq.setRecipient(event.getPhoneNumber());
            smsReq.setChannel(NotificationChannel.SMS);
            smsReq.setType(NotificationType.AUTH_VERIFICATION);
            smsReq.setTemplateCode("AUTH_OTP_SMS");
            smsReq.setTemplateVariables(vars);
            sendDirectNotification(smsReq);
        }
    }

    private String substituteVariables(String template, Map<String, String> variables) {
        if (template == null) return "";
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }
}
