package com.example.payment.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.payment.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    List<Payment> findByBookingId(String bookingId);
}