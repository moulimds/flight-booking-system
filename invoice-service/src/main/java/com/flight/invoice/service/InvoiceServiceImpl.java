package com.flight.invoice.service;

import com.flight.invoice.dto.InvoiceRequest;
import com.flight.invoice.dto.InvoiceResponse;
import com.flight.invoice.entity.Invoice;
import com.flight.invoice.exception.ResourceNotFoundException;
import com.flight.invoice.messaging.InvoiceEventPublisher;
import com.flight.invoice.messaging.InvoiceGeneratedEvent;
import com.flight.invoice.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceEventPublisher invoiceEventPublisher;

    @Override
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        logger.info("Creating invoice for booking id: {}", request.getBookingId());

        String invoiceNumber = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal tax = request.getTax() != null ? request.getTax() : request.getAmount().multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = request.getTotalAmount() != null ? request.getTotalAmount() : request.getAmount().add(tax);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setBookingId(request.getBookingId());
        invoice.setPaymentId(request.getPaymentId());
        invoice.setUserId(request.getUserId());
        invoice.setPassengerName(request.getPassengerName() != null ? request.getPassengerName() : "Passenger");
        invoice.setAmount(request.getAmount());
        invoice.setTax(tax);
        invoice.setTotalAmount(totalAmount);
        invoice.setInvoiceStatus("GENERATED");
        invoice.setGeneratedAt(LocalDateTime.now());

        Invoice saved = invoiceRepository.save(invoice);

        try {
            invoiceEventPublisher.publishInvoiceGenerated(new InvoiceGeneratedEvent(
                    saved.getId(),
                    saved.getInvoiceNumber(),
                    saved.getBookingId(),
                    saved.getPaymentId(),
                    saved.getUserId(),
                    saved.getTotalAmount(),
                    saved.getGeneratedAt()
            ));
        } catch (Exception e) {
            logger.warn("Failed to publish InvoiceGeneratedEvent: {}", e.getMessage());
        }

        return mapToInvoiceResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        return mapToInvoiceResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByBookingId(Long bookingId) {
        Invoice invoice = invoiceRepository.findFirstByBookingIdOrderByIdDesc(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for booking id: " + bookingId));
        return mapToInvoiceResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByUserId(Long userId) {
        return invoiceRepository.findByUserId(userId).stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceResponse cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        invoice.setInvoiceStatus("CANCELLED");
        Invoice updated = invoiceRepository.save(invoice);
        return mapToInvoiceResponse(updated);
    }

    @Override
    public void handlePaymentSuccess(Long paymentId, Long bookingId, Long userId, BigDecimal amount) {
        if (!invoiceRepository.existsByBookingId(bookingId)) {
            logger.info("Auto-generating invoice for bookingId: {}, paymentId: {}", bookingId, paymentId);
            InvoiceRequest request = new InvoiceRequest(
                    bookingId,
                    paymentId,
                    userId,
                    "Valued Passenger",
                    amount,
                    amount.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP),
                    amount.add(amount.multiply(new BigDecimal("0.10"))).setScale(2, RoundingMode.HALF_UP)
            );
            createInvoice(request);
        }
    }

    @Override
    public void handleBookingCancelled(Long bookingId) {
        invoiceRepository.findByBookingId(bookingId).ifPresent(invoice -> {
            logger.info("Marking invoice {} as CANCELLED for bookingId {}", invoice.getInvoiceNumber(), bookingId);
            invoice.setInvoiceStatus("CANCELLED");
            invoiceRepository.save(invoice);
        });
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getBookingId(),
                invoice.getPaymentId(),
                invoice.getUserId(),
                invoice.getPassengerName(),
                invoice.getAmount(),
                invoice.getTax(),
                invoice.getTotalAmount(),
                invoice.getInvoiceStatus(),
                invoice.getGeneratedAt()
        );
    }
}
