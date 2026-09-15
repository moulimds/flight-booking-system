package com.example.payment.controller;


import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.PaymentStatusRequest;
import com.example.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.initiatePayment(request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String paymentId) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(paymentId)
        );
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentByBooking(
            @PathVariable String bookingId) {

        return ResponseEntity.ok(
                paymentService.getPaymentByBooking(bookingId)
        );
    }

    @PostMapping("/{paymentId}/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable String paymentId) {

        return ResponseEntity.ok(
                paymentService.processPayment(paymentId)
        );
    }

    @PostMapping("/{paymentId}/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @PathVariable String paymentId) {

        return ResponseEntity.ok(
                paymentService.verifyPayment(paymentId)
        );
    }

    @PostMapping("/{paymentId}/retry")
    public ResponseEntity<PaymentResponse> retryPayment(
            @PathVariable String paymentId) {

        return ResponseEntity.ok(
                paymentService.retryPayment(paymentId)
        );
    }

    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updateStatus(
            @PathVariable String paymentId,
            @Valid @RequestBody PaymentStatusRequest request) {

        return ResponseEntity.ok(
                paymentService.updatePaymentStatus(
                        paymentId,
                        request
                )
        );
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {

        return ResponseEntity.ok(
                "Payment Service is running"
        );
    }
}