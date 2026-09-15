package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.PaymentStatusRequest;

import java.util.List;

public interface PaymentService {

    PaymentResponse initiatePayment(PaymentRequest request);

    PaymentResponse getPaymentById(String paymentId);

    List<PaymentResponse> getPaymentByBooking(String bookingId);

    PaymentResponse processPayment(String paymentId);

    PaymentResponse verifyPayment(String paymentId);

    PaymentResponse retryPayment(String paymentId);

    PaymentResponse updatePaymentStatus(
            String paymentId,
            PaymentStatusRequest request
    );
}