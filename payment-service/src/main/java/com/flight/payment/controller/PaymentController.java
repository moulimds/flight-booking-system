package com.flight.payment.controller;

import com.flight.payment.dto.PaymentProcessRequest;
import com.flight.payment.dto.PaymentRequest;
import com.flight.payment.dto.PaymentResponse;
import com.flight.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        logger.info("REST request to create payment for booking id: {}", request.getBookingId());
        PaymentResponse response = paymentService.createPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        logger.info("REST request to get all payments");
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable("id") Long id) {
        logger.info("REST request to get payment id: {}", id);
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getPaymentByBookingId(@PathVariable("bookingId") Long bookingId) {
        logger.info("REST request to get payment by booking id: {}", bookingId);
        PaymentResponse payment = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<PaymentResponse> getPaymentByReference(@PathVariable("reference") String reference) {
        logger.info("REST request to get payment by reference: {}", reference);
        PaymentResponse payment = paymentService.getPaymentByReference(reference);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable("id") Long id,
            @RequestBody(required = false) PaymentProcessRequest request) {
        logger.info("REST request to process payment id: {}", id);
        PaymentResponse response = paymentService.processPayment(id, request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/refund-request")
    public ResponseEntity<PaymentResponse> requestRefund(@PathVariable("id") Long id) {
        logger.info("REST request to request refund for payment id: {}", id);

        PaymentResponse response = paymentService.requestRefund(id);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable("id") Long id) {
        logger.info("REST request to refund payment id: {}", id);
        PaymentResponse response = paymentService.refundPayment(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable("id") Long id) {
        logger.info("REST request to cancel payment id: {}", id);
        PaymentResponse response = paymentService.cancelPayment(id);
        return ResponseEntity.ok(response);
    }
}
