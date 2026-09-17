package com.airline.notification.controller;

import com.airline.notification.dto.*;
import com.airline.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Operations", description = "Endpoints for direct multi-channel dispatches, audit logs, and asynchronous event simulation.")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send-direct")
    @Operation(summary = "Send direct notification", description = "Dispatches an email or SMS notification immediately using custom content or a pre-configured template.")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendDirectNotification(
            @Valid @RequestBody DirectNotificationRequest request) {
        NotificationResponse response = notificationService.sendDirectNotification(request);
        return ResponseEntity.ok(ApiResponse.success("Notification processed successfully", response));
    }

    @GetMapping("/logs/{id}")
    @Operation(summary = "Get notification log by ID", description = "Retrieves the dispatch audit record, status (SENT/FAILED), error details, and timestamp for a specific notification ID.")
    public ResponseEntity<ApiResponse<NotificationResponse>> getLogById(
            @Parameter(description = "Notification Log ID", required = true)
            @PathVariable Long id) {
        NotificationResponse response = notificationService.getLogById(id);
        return ResponseEntity.ok(ApiResponse.success("Notification log retrieved successfully", response));
    }

    @GetMapping("/logs/user/{userId}")
    @Operation(summary = "Get notification logs for a user", description = "Retrieves complete chronological history of notifications dispatched to a specific customer.")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getLogsByUserId(
            @Parameter(description = "Customer User ID, e.g. USER-1001", required = true)
            @PathVariable String userId) {
        List<NotificationResponse> responses = notificationService.getLogsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User notification history retrieved successfully", responses));
    }

    @PostMapping("/simulate-event/booking")
    @Operation(summary = "Simulate Booking RabbitMQ Event", description = "Simulates incoming booking confirmation or cancellation messages from RabbitMQ to trigger notifications.")
    public ResponseEntity<ApiResponse<String>> simulateBookingEvent(@RequestBody BookingEventDTO event) {
        notificationService.processBookingEvent(event);
        return ResponseEntity.ok(ApiResponse.success("Booking event simulated and notification dispatched", "Event processed"));
    }

    @PostMapping("/simulate-event/payment")
    @Operation(summary = "Simulate Payment RabbitMQ Event", description = "Simulates incoming payment receipt or refund messages from RabbitMQ.")
    public ResponseEntity<ApiResponse<String>> simulatePaymentEvent(@RequestBody PaymentEventDTO event) {
        notificationService.processPaymentEvent(event);
        return ResponseEntity.ok(ApiResponse.success("Payment event simulated and notification dispatched", "Event processed"));
    }

    @PostMapping("/simulate-event/flight-alert")
    @Operation(summary = "Simulate Flight Alert RabbitMQ Event", description = "Simulates flight delay, cancellation, or gate change broadcasts to affected passengers.")
    public ResponseEntity<ApiResponse<String>> simulateFlightAlertEvent(@RequestBody FlightAlertEventDTO event) {
        notificationService.processFlightAlertEvent(event);
        return ResponseEntity.ok(ApiResponse.success("Flight alert event simulated and notifications broadcasted", "Event processed"));
    }

    @PostMapping("/simulate-event/auth-verification")
    @Operation(summary = "Simulate Auth Verification OTP Event", description = "Simulates OTP generation event for user registration, login, or password resets.")
    public ResponseEntity<ApiResponse<String>> simulateAuthVerificationEvent(@RequestBody AuthVerificationDTO event) {
        notificationService.processAuthVerificationEvent(event);
        return ResponseEntity.ok(ApiResponse.success("Auth verification OTP dispatched", "Event processed"));
    }
}
