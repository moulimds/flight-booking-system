package com.airline.notification.config;

import com.airline.notification.entity.*;
import com.airline.notification.repository.NotificationLogRepository;
import com.airline.notification.repository.NotificationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final NotificationTemplateRepository templateRepository;
    private final NotificationLogRepository logRepository;

    public DataInitializer(NotificationTemplateRepository templateRepository, NotificationLogRepository logRepository) {
        this.templateRepository = templateRepository;
        this.logRepository = logRepository;
    }

    @Override
    public void run(String... args) {
        if (templateRepository.count() > 0) {
            log.info("Notification templates already initialized.");
            return;
        }

        log.info("Seeding notification templates and initial demo logs...");

        NotificationTemplate t1 = new NotificationTemplate(
                "BOOKING_CONFIRMATION_EMAIL",
                "Flight Booking Confirmed - Ref: {{bookingReference}}",
                "Dear {{name}},\n\nYour flight booking with reference {{bookingReference}} is confirmed!\n\nFlight: {{flightNumber}}\nSeats: {{seatNumbers}}\nDeparture: {{departureTime}}\nTotal Paid: ${{totalPrice}}\n\nThank you for flying with us!\nSkyWings Airlines Customer Support",
                NotificationChannel.EMAIL,
                true
        );

        NotificationTemplate t2 = new NotificationTemplate(
                "BOOKING_CANCELLATION_EMAIL",
                "Flight Booking Cancelled - Ref: {{bookingReference}}",
                "Dear {{name}},\n\nYour booking {{bookingReference}} for Flight {{flightNumber}} has been successfully cancelled.\n\nRefund processing has been initiated.\n\nRegards,\nSkyWings Airlines Support",
                NotificationChannel.EMAIL,
                true
        );

        NotificationTemplate t3 = new NotificationTemplate(
                "PAYMENT_RECEIPT_EMAIL",
                "Payment Receipt - Ref: {{bookingReference}}",
                "Receipt for Booking Reference: {{bookingReference}}\nTransaction ID: {{paymentId}}\nAmount: {{currency}} {{amount}}\nStatus: {{status}}\nProcessed at: {{transactionTime}}\n\nThank you for choosing SkyWings Airlines.",
                NotificationChannel.EMAIL,
                true
        );

        NotificationTemplate t4 = new NotificationTemplate(
                "AUTH_OTP_EMAIL",
                "Your Security Verification Code: {{otpCode}}",
                "Hello {{name}},\n\nYour one-time verification code for {{verificationType}} is: {{otpCode}}\n\nThis code will expire in {{expiresInMinutes}} minutes.\nIf you did not request this, please secure your account immediately.",
                NotificationChannel.EMAIL,
                true
        );

        NotificationTemplate t5 = new NotificationTemplate(
                "AUTH_OTP_SMS",
                null,
                "SkyWings Alert: Your verification code is {{otpCode}}. Valid for {{expiresInMinutes}} mins. Do NOT share this code with anyone.",
                NotificationChannel.SMS,
                true
        );

        templateRepository.saveAll(List.of(t1, t2, t3, t4, t5));

        // Seed some sample notification logs for testing
        NotificationLog l1 = new NotificationLog(
                "USER-1001",
                "john.doe@example.com",
                NotificationChannel.EMAIL,
                NotificationType.BOOKING_CONFIRMATION,
                "Flight Booking Confirmed - Ref: BK-9901",
                "Dear John Doe,\nYour booking BK-9901 for Flight FL-101 is confirmed.",
                NotificationStatus.SENT,
                null,
                "{\"bookingReference\":\"BK-9901\",\"flightNumber\":\"FL-101\"}"
        );

        NotificationLog l2 = new NotificationLog(
                "USER-1001",
                "+15550198822",
                NotificationChannel.SMS,
                NotificationType.PAYMENT_ALERT,
                null,
                "SkyWings Alert: Payment of $450.00 for booking BK-9901 received successfully.",
                NotificationStatus.SENT,
                null,
                "{\"amount\":\"450.00\",\"currency\":\"USD\"}"
        );

        logRepository.saveAll(List.of(l1, l2));

        log.info("Initialized {} notification templates and {} sample logs.",
                templateRepository.count(), logRepository.count());
    }
}
