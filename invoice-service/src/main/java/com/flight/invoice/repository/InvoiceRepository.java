package com.flight.invoice.repository;

import com.flight.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByBookingId(Long bookingId);
    Optional<Invoice> findFirstByBookingIdOrderByIdDesc(Long bookingId);
    List<Invoice> findByUserId(Long userId);
    boolean existsByBookingId(Long bookingId);
}
