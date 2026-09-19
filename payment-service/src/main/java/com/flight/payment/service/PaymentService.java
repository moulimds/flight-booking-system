package com.flight.payment.service;

import com.flight.payment.dto.PaymentProcessRequest;
import com.flight.payment.dto.PaymentRequest;
import com.flight.payment.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    List<PaymentResponse> getAllPayments();
    PaymentResponse getPaymentById(Long id);
    PaymentResponse getPaymentByBookingId(Long bookingId);
    PaymentResponse getPaymentByReference(String reference);
    PaymentResponse processPayment(Long id, PaymentProcessRequest request);
    PaymentResponse requestRefund(Long id);
    PaymentResponse refundPayment(Long id);
    PaymentResponse cancelPayment(Long id);
    void handleBookingCancelled(Long bookingId);
    default void handleBookingCancelled(Long bookingId, java.math.BigDecimal refundAmount) {
        handleBookingCancelled(bookingId);
    }
}
