package com.flight.notification.controller;

import com.flight.notification.dto.NotificationRequest;
import com.flight.notification.dto.NotificationResponse;
import com.flight.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @PostMapping({"", "/", "/email", "/send"})
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        logger.info("REST request to send notification for user id: {}, recipient: {}", request.getUserId(), request.getRecipient());
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/registration", "/user-registration"})
    public ResponseEntity<NotificationResponse> notifyRegistration(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Registration Successful - Welcome to SkyWings");
        if (request.getMessage() == null) request.setMessage("Your account registration with email " + request.getRecipient() + " succeeded! Welcome aboard.");
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/booking", "/booking-confirmation"})
    public ResponseEntity<NotificationResponse> notifyBooking(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Booking Confirmed: " + request.getBookingReference());
        if (request.getMessage() == null) request.setMessage("Great news! Your booking " + request.getBookingReference() + " for flight " + request.getFlightNumber() + " is confirmed.");
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/cancellation", "/booking-cancellation"})
    public ResponseEntity<NotificationResponse> notifyCancellation(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Booking Cancellation: " + request.getBookingReference());
        if (request.getMessage() == null) request.setMessage("Your booking " + request.getBookingReference() + " has been cancelled.");
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/payment-success", "/payment/success"})
    public ResponseEntity<NotificationResponse> notifyPaymentSuccess(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Payment Successful: " + request.getPaymentReference());
        if (request.getMessage() == null) request.setMessage("Payment of " + request.getAmount() + " " + request.getCurrency() + " was successfully completed for Booking " + request.getBookingReference() + ".");
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/payment-failure", "/payment/failure"})
    public ResponseEntity<NotificationResponse> notifyPaymentFailure(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Payment Failed: " + request.getPaymentReference());
        if (request.getMessage() == null) request.setMessage("Payment transaction failed. Reason: " + (request.getReason() != null ? request.getReason() : "Card declined"));
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/invoice", "/bill-invoice"})
    public ResponseEntity<NotificationResponse> notifyInvoice(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Bill / Invoice: " + request.getInvoiceNumber());
        if (request.getMessage() == null) request.setMessage("Your official tax invoice " + request.getInvoiceNumber() + " totaling " + request.getAmount() + " " + request.getCurrency() + " is ready.");
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/refund-initialization", "/refund-initiation", "/refund/init"})
    public ResponseEntity<NotificationResponse> notifyRefundInit(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Refund Initialization");
        if (request.getMessage() == null) request.setMessage("Your refund request of amount " + request.getAmount() + " " + request.getCurrency() + " has been initiated.");
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/refund-processing", "/refund-process", "/refund/processing"})
    public ResponseEntity<NotificationResponse> notifyRefundProcess(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Refund Processing");
        if (request.getMessage() == null) request.setMessage("Your refund of amount " + request.getAmount() + " " + request.getCurrency() + " is being processed with your bank.");
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/refund-success", "/refund/success"})
    public ResponseEntity<NotificationResponse> notifyRefundSuccess(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Refund Success");
        if (request.getMessage() == null) request.setMessage("Your refund of amount " + request.getAmount() + " " + request.getCurrency() + " has been credited to your account.");
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/password-reset-otp", "/otp"})
    public ResponseEntity<NotificationResponse> notifyOtp(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Password Reset OTP");
        if (request.getMessage() == null) request.setMessage("Your OTP for password reset is: " + (request.getOtp() != null ? request.getOtp() : "784912") + ". Valid for 10 minutes.");
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/password-change", "/password/change", "/password-changed"})
    public ResponseEntity<NotificationResponse> notifyPasswordChange(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Security Notice: Password Changed Successfully");
        if (request.getMessage() == null) request.setMessage("Your account password has been changed successfully. If you did not make this change, please contact SkyWings support immediately.");
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    @PostMapping({"/reschedule", "/rescheduling", "/flight-reschedule"})
    public ResponseEntity<NotificationResponse> notifyReschedule(@RequestBody NotificationRequest request) {
        enrichRequestDetails(request);
        if (request.getSubject() == null) request.setSubject("Flight Rescheduled: " + request.getFlightNumber());
        if (request.getMessage() == null) request.setMessage("Your booking " + request.getBookingReference() + " has been rescheduled to " + request.getDepartureDate() + " at " + request.getDepartureTime() + ". Assigned seat: " + request.getSeatNumber());
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    private void enrichRequestDetails(NotificationRequest request) {
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            request.setCustomerName("Sindhu Sakthivel");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            request.setUsername("sindhu123");
        }
        if (request.getRegistrationDateTime() == null || request.getRegistrationDateTime().trim().isEmpty()) {
            request.setRegistrationDateTime("17-Sep-2026, 5:50 PM");
        }
        if (request.getAccountStatus() == null || request.getAccountStatus().trim().isEmpty()) {
            request.setAccountStatus("ACTIVE / PENDING_VERIFICATION");
        }
        if (request.getVerificationLink() == null || request.getVerificationLink().trim().isEmpty()) {
            request.setVerificationLink("482913 or https://skywings.com/verify?otp=482913");
        }
        if (request.getOtpExpiry() == null || request.getOtpExpiry().trim().isEmpty()) {
            request.setOtpExpiry("10 minutes");
        }
        if (request.getCustomerId() == null || request.getCustomerId().trim().isEmpty()) {
            request.setCustomerId("CUST10045");
        }
        if (request.getSecurityMessage() == null || request.getSecurityMessage().trim().isEmpty()) {
            request.setSecurityMessage("Don't share OTP/password with anyone.");
        }
        if (request.getSecurityWarning() == null || request.getSecurityWarning().trim().isEmpty()) {
            request.setSecurityWarning("Do not share this OTP with anyone, including SkyWings staff.");
        }
        if (request.getFlightNumber() == null || request.getFlightNumber().trim().isEmpty()) {
            request.setFlightNumber("AI101");
        }
        if (request.getFlightName() == null || request.getFlightName().trim().isEmpty()) {
            request.setFlightName("Air India");
        }
        if (request.getFlightId() == null) {
            request.setFlightId(1L);
        }
        if (request.getOrigin() == null || request.getOrigin().trim().isEmpty()) {
            request.setOrigin("DEL (New Delhi - Terminal 3)");
        }
        if (request.getDestination() == null || request.getDestination().trim().isEmpty()) {
            request.setDestination("BOM (Mumbai - Terminal 2)");
        }
        if (request.getDepartureDate() == null || request.getDepartureDate().trim().isEmpty()) {
            request.setDepartureDate("17-Sep-2026");
        }
        if (request.getDepartureTime() == null || request.getDepartureTime().trim().isEmpty()) {
            request.setDepartureTime("05:50 PM");
        }
        if (request.getArrivalDate() == null || request.getArrivalDate().trim().isEmpty()) {
            request.setArrivalDate("17-Sep-2026");
        }
        if (request.getArrivalTime() == null || request.getArrivalTime().trim().isEmpty()) {
            request.setArrivalTime("08:15 PM");
        }
        if (request.getSeatNumber() == null || request.getSeatNumber().trim().isEmpty()) {
            request.setSeatNumber("14A");
        }
        if (request.getSeatClass() == null || request.getSeatClass().trim().isEmpty()) {
            request.setSeatClass("ECONOMY");
        }
        if (request.getPassengerCount() == null) {
            request.setPassengerCount(1);
        }
        if (request.getBookingReference() == null || request.getBookingReference().trim().isEmpty()) {
            request.setBookingReference(request.getBookingId() != null ? ("BK" + request.getBookingId()) : "BK1001");
        }
        if (request.getBookingStatus() == null || request.getBookingStatus().trim().isEmpty()) {
            request.setBookingStatus("CONFIRMED");
        }
        if (request.getPaymentReference() == null || request.getPaymentReference().trim().isEmpty()) {
            request.setPaymentReference("PAY1001");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            request.setPaymentMethod("CARD / UPI Instant");
        }
        if (request.getTransactionDateTime() == null || request.getTransactionDateTime().trim().isEmpty()) {
            request.setTransactionDateTime("17-Sep-2026, 05:50 PM");
        }
        if (request.getInvoiceNumber() == null || request.getInvoiceNumber().trim().isEmpty()) {
            request.setInvoiceNumber("INV-2026-991");
        }
        if (request.getAmount() == null) {
            request.setAmount(5400.00);
        }
        if (request.getTotalAmount() == null) {
            request.setTotalAmount(request.getAmount());
        }
        if (request.getBaseFare() == null) {
            request.setBaseFare(request.getAmount() * 0.85);
        }
        if (request.getTaxes() == null) {
            request.setTaxes(request.getAmount() * 0.15);
        }
        if (request.getCancellationFee() == null) {
            request.setCancellationFee(1080.00);
        }
        if (request.getRefundAmount() == null) {
            request.setRefundAmount(4320.00);
        }
        if (request.getCancellationStatus() == null || request.getCancellationStatus().trim().isEmpty()) {
            request.setCancellationStatus("CANCELLED / REFUND_SCHEDULED");
        }
        if (request.getRefundId() == null || request.getRefundId().trim().isEmpty()) {
            request.setRefundId("REF1001");
        }
        if (request.getExpectedCreditInfo() == null || request.getExpectedCreditInfo().trim().isEmpty()) {
            request.setExpectedCreditInfo("Credited to source account/card ending in 4111 within 24 hours");
        }
        if (request.getExpectedTimeframe() == null || request.getExpectedTimeframe().trim().isEmpty()) {
            request.setExpectedTimeframe("3 to 5 business days");
        }
        if (request.getDeviceInfo() == null || request.getDeviceInfo().trim().isEmpty()) {
            request.setDeviceInfo("Chrome / Windows 11 (IP: 192.168.1.10)");
        }
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        logger.info("REST request to get all notifications");
        List<NotificationResponse> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable("id") Long id) {
        logger.info("REST request to get notification id: {}", id);
        NotificationResponse notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(@PathVariable("userId") Long userId) {
        logger.info("REST request to get notifications for user id: {}", userId);
        List<NotificationResponse> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByBookingId(@PathVariable("bookingId") Long bookingId) {
        logger.info("REST request to get notifications for booking id: {}", bookingId);
        List<NotificationResponse> notifications = notificationService.getNotificationsByBookingId(bookingId);
        return ResponseEntity.ok(notifications);
    }
}
