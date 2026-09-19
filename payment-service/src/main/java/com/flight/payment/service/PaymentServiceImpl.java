package com.flight.payment.service;

import com.flight.payment.dto.PaymentProcessRequest;
import com.flight.payment.dto.PaymentRequest;
import com.flight.payment.dto.PaymentResponse;
import com.flight.payment.entity.Payment;
import com.flight.payment.exception.InvalidPaymentException;
import com.flight.payment.exception.PaymentFailedException;
import com.flight.payment.exception.ResourceNotFoundException;
import com.flight.payment.messaging.PaymentEventPublisher;
import com.flight.payment.messaging.PaymentFailedEvent;
import com.flight.payment.messaging.PaymentRefundedEvent;
import com.flight.payment.messaging.PaymentSuccessfulEvent;
import com.flight.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentEventPublisher paymentEventPublisher;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        logger.info("Creating payment for booking id: {}", request.getBookingId());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Payment amount must be greater than zero");
        }

        String reference = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment();
        payment.setPaymentReference(reference);
        payment.setBookingId(request.getBookingId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR");
        payment.setPaymentMethod(request.getPaymentMethod().toUpperCase());
        payment.setPaymentStatus("INITIATED");
        payment.setTransactionDate(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        return mapToPaymentResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findFirstByBookingIdOrderByIdDesc(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking id: " + bookingId));
        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String reference) {
        Payment payment = paymentRepository.findByPaymentReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with reference: " + reference));
        return mapToPaymentResponse(payment);
    }

    @Override
    public PaymentResponse processPayment(Long id, PaymentProcessRequest request) {
        logger.info("Processing simulated payment for id: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        boolean isSuccess = request == null || request.getSimulatedStatus() == null ||
                "SUCCESS".equalsIgnoreCase(request.getSimulatedStatus());

        payment.setTransactionDate(LocalDateTime.now());

        if (isSuccess) {
            payment.setPaymentStatus("SUCCESS");
            Payment saved = paymentRepository.save(payment);

            try {
                paymentEventPublisher.publishPaymentSuccess(new PaymentSuccessfulEvent(
                        saved.getId(),
                        saved.getPaymentReference(),
                        saved.getBookingId(),
                        saved.getUserId(),
                        saved.getAmount(),
                        saved.getCurrency(),
                        saved.getPaymentMethod()
                ));
            } catch (Exception e) {
                logger.warn("Failed to publish PaymentSuccessfulEvent: {}", e.getMessage());
            }

            return mapToPaymentResponse(saved);
        } else {
            payment.setPaymentStatus("FAILED");
            Payment saved = paymentRepository.save(payment);

            String reason = (request != null && request.getFailureReason() != null)
                    ? request.getFailureReason()
                    : "Simulated payment failure (insufficient funds / bank declined)";

            try {
                paymentEventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                        saved.getId(),
                        saved.getPaymentReference(),
                        saved.getBookingId(),
                        saved.getUserId(),
                        saved.getAmount(),
                        reason
                ));
            } catch (Exception e) {
                logger.warn("Failed to publish PaymentFailedEvent: {}", e.getMessage());
            }

            throw new PaymentFailedException("Payment processing failed: " + reason);
        }
    }
    @Override
    public PaymentResponse requestRefund(Long id) {
        logger.info("Customer requesting refund for payment id: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found with id: " + id));

        if (!"SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new InvalidPaymentException(
                    "Refund can be requested only for successful payments");
        }

        payment.setPaymentStatus("REFUND_REQUESTED");

        Payment updated = paymentRepository.save(payment);

        logger.info("Refund requested successfully for payment id: {}", id);

        return mapToPaymentResponse(updated);
    }
    @Override
    public PaymentResponse refundPayment(Long id) {
        logger.info("Refunding payment id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if ("REFUNDED".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new InvalidPaymentException("Payment is already refunded");
        }

        payment.setPaymentStatus("REFUNDED");
        Payment updated = paymentRepository.save(payment);

        try {
            paymentEventPublisher.publishPaymentRefunded(new PaymentRefundedEvent(
                    updated.getId(),
                    updated.getPaymentReference(),
                    updated.getBookingId(),
                    updated.getUserId(),
                    updated.getAmount()
            ));
        } catch (Exception e) {
            logger.warn("Failed to publish PaymentRefundedEvent: {}", e.getMessage());
        }

        return mapToPaymentResponse(updated);
    }

    @Override
    public PaymentResponse cancelPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        payment.setPaymentStatus("CANCELLED");
        Payment updated = paymentRepository.save(payment);
        return mapToPaymentResponse(updated);
    }

    @Override
    public void handleBookingCancelled(Long bookingId) {
        handleBookingCancelled(bookingId, null);
    }

    @Override
    public void handleBookingCancelled(Long bookingId, java.math.BigDecimal refundAmount) {
        paymentRepository.findByBookingId(bookingId).ifPresent(payment -> {
            if ("SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
                logger.info("Auto-refunding payment {} for cancelled booking {}, calculated refund: {}",
                        payment.getId(), bookingId, refundAmount);
                payment.setPaymentStatus("REFUNDED");
                Payment updated = paymentRepository.save(payment);

                java.math.BigDecimal finalAmount = refundAmount != null ? refundAmount : updated.getAmount();
                try {
                    paymentEventPublisher.publishPaymentRefunded(new PaymentRefundedEvent(
                            updated.getId(),
                            updated.getPaymentReference(),
                            updated.getBookingId(),
                            updated.getUserId(),
                            finalAmount
                    ));
                } catch (Exception e) {
                    logger.warn("Failed to publish PaymentRefundedEvent: {}", e.getMessage());
                }
            }
        });
    }
   
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getTransactionDate(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
