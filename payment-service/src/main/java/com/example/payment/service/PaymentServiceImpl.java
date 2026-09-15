package com.example.payment.service;


import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.PaymentStatusRequest;
import com.example.payment.entity.Payment;
import com.example.payment.enums.PaymentStatus;
import com.example.payment.event.PaymentEventPublisher;
import com.example.payment.exceptiom.InvalidPaymentException;
import com.example.payment.exceptiom.PaymentNotFoundException;
import com.example.payment.gateway.MockPaymentGateway;
import com.example.payment.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MockPaymentGateway paymentGateway;
    private final PaymentEventPublisher eventPublisher;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            MockPaymentGateway paymentGateway,
            PaymentEventPublisher eventPublisher) {

        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {

        Payment payment = Payment.builder()
                .paymentId("PAY-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase())
                .bookingId(request.bookingId())
                .customerId(request.customerId())
                .amount(request.amount())
                .currency(request.currency())
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(String paymentId) {

        Payment payment = findPayment(paymentId);

        return mapToResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentByBooking(String bookingId) {

        List<Payment> payments = paymentRepository.findByBookingId(bookingId);

        if (payments.isEmpty()) {
            throw new PaymentNotFoundException(
                    "No payment found for booking: " + bookingId
            );
        }

        return payments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PaymentResponse processPayment(String paymentId) {

        Payment payment = findPayment(paymentId);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new InvalidPaymentException(
                    "Payment has already been completed"
            );
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new InvalidPaymentException(
                    "Cancelled payment cannot be processed"
            );
        }

        String result = paymentGateway.processPayment(payment);

        if ("FAILED".equals(result)) {

            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());

            Payment saved = paymentRepository.save(payment);

            eventPublisher.publishPaymentFailed(saved);

            return mapToResponse(saved);
        }

        payment.setTransactionId(result);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        eventPublisher.publishPaymentSuccess(saved);

        return mapToResponse(saved);
    }

    @Override
    public PaymentResponse verifyPayment(String paymentId) {

        Payment payment = findPayment(paymentId);

        if (payment.getTransactionId() == null) {
            throw new InvalidPaymentException(
                    "Payment does not have a transaction ID"
            );
        }

        boolean verified =
                paymentGateway.verifyPayment(
                        payment.getTransactionId()
                );

        if (!verified) {

            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            throw new InvalidPaymentException(
                    "Payment verification failed"
            );
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse retryPayment(String paymentId) {

        Payment payment = findPayment(paymentId);

        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new InvalidPaymentException(
                    "Only failed payments can be retried"
            );
        }

        payment.setStatus(PaymentStatus.PENDING);
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        return processPayment(paymentId);
    }

    @Override
    public PaymentResponse updatePaymentStatus(
            String paymentId,
            PaymentStatusRequest request) {

        Payment payment = findPayment(paymentId);

        payment.setStatus(request.status());
        payment.setUpdatedAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        return mapToResponse(saved);
    }

    private Payment findPayment(String paymentId) {

        return paymentRepository
                .findByPaymentId(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found: " + paymentId
                        ));
    }

    private PaymentResponse mapToResponse(Payment payment) {

        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getBookingId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}