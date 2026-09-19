package com.flight.notification.messaging;

import com.flight.notification.dto.NotificationRequest;
import com.flight.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = "notification.events.queue")
    public void handleNotificationEvents(Map<String, Object> payload, Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        logger.info("Received event on notification queue with routingKey: {}, payload: {}", routingKey, payload);

        try {
            Long userId = extractLong(payload.get("userId"));
            Long bookingId = extractLong(payload.get("bookingId"));
            String recipient = payload.get("email") != null ? (String) payload.get("email") :
                    (payload.get("passengerEmail") != null ? (String) payload.get("passengerEmail") : "customer@flight.com");

            String subject = "Event Notification: " + routingKey;
            String content = "Notification triggered for event: " + routingKey + ". Details: " + payload.toString();

            if (routingKey.contains("user.registered") || routingKey.contains("user.registration") || routingKey.contains("auth.user.registered")) {
                subject = "Registration Successful - Welcome to Flight Booking System";
                content = "Hello " + (payload.get("username") != null ? payload.get("username") : "Customer") + ", your account registration succeeded! Welcome aboard.";
            } else if (routingKey.contains("booking.confirmed") || routingKey.contains("booking.created")) {
                subject = "Booking Confirmation: " + payload.get("bookingReference");
                content = "Great news! Your booking " + payload.get("bookingReference") + " for seat " + payload.get("seatNumber") + " is successfully confirmed.";
            } else if (routingKey.contains("booking.cancelled")) {
                subject = "Booking Cancellation & Refund: " + payload.get("bookingReference");
                Object refundPct = payload.get("refundPercentage");
                Object refundAmt = payload.get("refundAmount");
                Object cancelFee = payload.get("cancellationFee");
                Object fareAmt = payload.get("fareAmount");
                if (refundPct != null) {
                    content = "Your booking " + payload.get("bookingReference") + " has been cancelled under our Tiered Refund Policy ("
                            + refundPct + "% Refund Tier). Total Fare: " + fareAmt
                            + " INR | Cancellation Fee: " + cancelFee
                            + " INR | Refund Amount Credited: " + refundAmt
                            + " INR. The refund has been initiated and will credit to your account within 3-5 business days.";
                } else {
                    content = "Your booking " + payload.get("bookingReference") + " has been cancelled. Any eligible refund has been scheduled.";
                }
            } else if (routingKey.contains("payment.success") || routingKey.contains("payment.successful")) {
                subject = "Payment Successful: " + payload.get("paymentReference");
                content = "Your payment of " + payload.get("amount") + " " + (payload.get("currency") != null ? payload.get("currency") : "INR") + " was successful.";
            } else if (routingKey.contains("payment.failed") || routingKey.contains("payment.failure")) {
                subject = "Payment Failed: " + payload.get("paymentReference");
                content = "Your payment for booking ID " + bookingId + " has failed. Reason: " + (payload.get("reason") != null ? payload.get("reason") : "Transaction declined");
            } else if (routingKey.contains("invoice.generated") || routingKey.contains("invoice.created")) {
                subject = "Bill / Invoice: " + payload.get("invoiceNumber");
                content = "Your invoice " + payload.get("invoiceNumber") + " for amount " + (payload.get("amount") != null ? payload.get("amount") : payload.get("totalAmount")) + " has been generated.";
            } else if (routingKey.contains("refund.init") || routingKey.contains("refund.initialization")) {
                subject = "Refund Initialization: " + payload.get("refundReference");
                content = "Your refund request of amount " + payload.get("amount") + " for booking ID " + bookingId + " has been initiated.";
            } else if (routingKey.contains("refund.proc") || routingKey.contains("refund.processing")) {
                subject = "Refund Processing: " + payload.get("refundReference");
                content = "Your refund of amount " + payload.get("amount") + " for booking ID " + bookingId + " is currently processing with your bank/card issuer.";
            } else if (routingKey.contains("refund.success") || routingKey.contains("refund.comp") || routingKey.contains("payment.refunded")) {
                subject = "Refund Success: " + payload.get("refundReference");
                content = "Your refund of amount " + payload.get("amount") + " for booking ID " + bookingId + " has been successfully completed and credited.";
            } else if (routingKey.contains("otp") || routingKey.contains("password.reset") || routingKey.contains("password-reset")) {
                subject = "Password Reset OTP";
                content = "Your one-time password (OTP) for password reset is: " + (payload.get("otp") != null ? payload.get("otp") : "123456") + ". Valid for 10 minutes.";
            } else if (routingKey.contains("resched") || routingKey.contains("flight.rescheduled") || routingKey.contains("booking.rescheduled")) {
                subject = "Flight / Booking Rescheduled: " + (payload.get("bookingReference") != null ? payload.get("bookingReference") : payload.get("flightNumber"));
                content = "Your flight has been rescheduled! New departure time: " + (payload.get("newDepartureTime") != null ? payload.get("newDepartureTime") : payload.get("departureTime")) + ", Flight: " + payload.get("flightNumber") + ", Seat: " + payload.get("seatNumber");
            } else if (routingKey.contains("checkin.completed")) {
                subject = "Check-in Successful! Boarding Pass: " + payload.get("boardingPassNumber");
                content = "You are checked in! Seat: " + payload.get("seatNumber") + ", Boarding Pass: " + payload.get("boardingPassNumber");
            }

            NotificationRequest request = new NotificationRequest(
                    userId,
                    bookingId,
                    "EMAIL",
                    recipient,
                    subject,
                    content,
                    "SENT"
            );

            notificationService.sendNotification(request);
        } catch (Exception e) {
            logger.error("Error creating notification for routingKey {}: {}", routingKey, e.getMessage());
        }
    }

    private Long extractLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        } else if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
