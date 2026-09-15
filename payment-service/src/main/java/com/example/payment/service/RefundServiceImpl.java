package com.example.payment.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.payment.dto.RefundRequest;
import com.example.payment.dto.RefundResponse;
import com.example.payment.entity.Payment;
import com.example.payment.entity.Refund;
import com.example.payment.enums.PaymentStatus;
import com.example.payment.enums.RefundStatus;
import com.example.payment.event.PaymentEventPublisher;
import com.example.payment.exceptiom.InvalidPaymentException;
import com.example.payment.exceptiom.PaymentNotFoundException;
import com.example.payment.exceptiom.RefundNotFoundException;
import com.example.payment.gateway.MockPaymentGateway;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.RefundRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final MockPaymentGateway paymentGateway;
    private final PaymentEventPublisher eventPublisher;

    public RefundServiceImpl(
            RefundRepository refundRepository,
            PaymentRepository paymentRepository,
            MockPaymentGateway paymentGateway,
            PaymentEventPublisher eventPublisher) {

        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RefundResponse initiateRefund(RefundRequest request) {

        Payment payment = paymentRepository
                .findByPaymentId(request.paymentId())
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found: " + request.paymentId()
                        ));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentException(
                    "Only successful payments can be refunded"
            );
        }

        if (!payment.getBookingId().equals(request.bookingId())) {
            throw new InvalidPaymentException(
                    "Booking ID does not match payment"
            );
        }

        if (request.amount().compareTo(payment.getAmount()) > 0) {
            throw new InvalidPaymentException(
                    "Refund amount cannot exceed payment amount"
            );
        }

        Refund refund = Refund.builder()
                .refundId("REF-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase())
                .paymentId(request.paymentId())
                .bookingId(request.bookingId())
                .amount(request.amount())
                .reason(request.reason())
                .status(RefundStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapToResponse(refundRepository.save(refund));
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefund(String refundId) {

        Refund refund = findRefund(refundId);

        return mapToResponse(refund);
    }

    @Override
    public RefundResponse processRefund(String refundId) {

        Refund refund = findRefund(refundId);

        if (refund.getStatus() == RefundStatus.SUCCESS) {
            throw new InvalidPaymentException(
                    "Refund has already been processed"
            );
        }

        Payment payment = paymentRepository
                .findByPaymentId(refund.getPaymentId())
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found"
                        ));

        refund.setStatus(RefundStatus.PROCESSING);
        refund.setUpdatedAt(LocalDateTime.now());

        refundRepository.save(refund);

        String refundTransactionId =
                paymentGateway.processRefund(
                        payment.getTransactionId()
                );

        refund.setRefundTransactionId(refundTransactionId);
        refund.setStatus(RefundStatus.SUCCESS);
        refund.setUpdatedAt(LocalDateTime.now());

        Refund saved = refundRepository.save(refund);

        eventPublisher.publishRefundSuccess(saved);

        return mapToResponse(saved);
    }

    private Refund findRefund(String refundId) {

        return refundRepository
                .findByRefundId(refundId)
                .orElseThrow(() ->
                        new RefundNotFoundException(
                                "Refund not found: " + refundId
                        ));
    }

    private RefundResponse mapToResponse(Refund refund) {

        return new RefundResponse(
                refund.getRefundId(),
                refund.getPaymentId(),
                refund.getBookingId(),
                refund.getAmount(),
                refund.getReason(),
                refund.getRefundTransactionId(),
                refund.getStatus(),
                refund.getCreatedAt(),
                refund.getUpdatedAt()
        );
    }
}