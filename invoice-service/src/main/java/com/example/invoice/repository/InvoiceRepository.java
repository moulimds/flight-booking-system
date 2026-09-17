package com.example.invoice.repository;

import com.example.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository
        extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceId(String invoiceId);

    List<Invoice> findByBookingId(String bookingId);

    List<Invoice> findByCustomerId(String customerId);

    boolean existsByBookingId(String bookingId);

}