package com.flight.payment.repository;

import com.flight.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentReference(String paymentReference);
    Optional<Payment> findFirstByBookingIdOrderByIdDesc(Long bookingId);
    List<Payment> findAllByBookingId(Long bookingId);
    Optional<Payment> findByBookingId(Long bookingId);
    List<Payment> findByUserId(Long userId);
}
