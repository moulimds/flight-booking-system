package com.example.payment.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.example.payment.entity.Refund;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByRefundId(String refundId);
}