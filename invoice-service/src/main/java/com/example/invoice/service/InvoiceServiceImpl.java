package com.example.invoice.service;

import com.example.invoice.dto.InvoiceRequest;
import com.example.invoice.dto.InvoiceResponse;
import com.example.invoice.entity.Invoice;
import com.example.invoice.enums.InvoiceStatus;
import com.example.invoice.exception.InvoiceNotFoundException;
import com.example.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl
        implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    private static final AtomicLong INVOICE_SEQUENCE =
            new AtomicLong(1);


    @Override
    public String health() {

        return "Invoice Service is running";

    }


    @Override
    public InvoiceResponse createInvoice(
            InvoiceRequest request) {

        Invoice invoice = Invoice.builder()

                .invoiceId(generateInvoiceId())

                .invoiceNumber(generateInvoiceNumber())

                .bookingId(request.bookingId())

                .paymentId(request.paymentId())

                .customerId(request.customerId())

                .customerName(request.customerName())

                .customerEmail(request.customerEmail())

                .flightNumber(request.flightNumber())

                .source(request.source())

                .destination(request.destination())

                .travelDate(request.travelDate())

                .baseFare(request.baseFare())

                .taxAmount(request.taxAmount())

                .serviceFee(request.serviceFee())

                .discount(request.discount())

                .totalAmount(request.totalAmount())

                .currency(request.currency())

                .transactionId(request.transactionId())

                .paymentMethod(request.paymentMethod())

                .paymentStatus(
                        request.paymentStatus() == null
                                ? "PENDING"
                                : request.paymentStatus()
                )

                .invoiceStatus(
                        "SUCCESS".equalsIgnoreCase(
                                request.paymentStatus()
                        )
                                ? InvoiceStatus.PAID
                                : InvoiceStatus.GENERATED
                )

                .build();


        Invoice savedInvoice =
                invoiceRepository.save(invoice);


        return convertToResponse(savedInvoice);
    }


    @Override
    public InvoiceResponse getInvoiceById(
            String invoiceId) {

        Invoice invoice =
                invoiceRepository
                        .findByInvoiceId(invoiceId)
                        .orElseThrow(() ->
                                new InvoiceNotFoundException(
                                        "Invoice not found: "
                                                + invoiceId
                                )
                        );

        return convertToResponse(invoice);
    }


    @Override
    public List<InvoiceResponse> getInvoicesByBooking(
            String bookingId) {

        return invoiceRepository
                .findByBookingId(bookingId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    @Override
    public List<InvoiceResponse> getInvoicesByCustomer(
            String customerId) {

        return invoiceRepository
                .findByCustomerId(customerId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    @Override
    public InvoiceResponse updateStatus(
            String invoiceId,
            InvoiceStatus status) {

        Invoice invoice =
                invoiceRepository
                        .findByInvoiceId(invoiceId)
                        .orElseThrow(() ->
                                new InvoiceNotFoundException(
                                        "Invoice not found: "
                                                + invoiceId
                                )
                        );


        invoice.setInvoiceStatus(status);


        if (status == InvoiceStatus.PAID) {

            invoice.setPaymentStatus("SUCCESS");

        } else if (status == InvoiceStatus.REFUNDED) {

            invoice.setPaymentStatus("REFUNDED");

        } else if (status == InvoiceStatus.CANCELLED) {

            invoice.setPaymentStatus("CANCELLED");

        }


        Invoice updated =
                invoiceRepository.save(invoice);


        return convertToResponse(updated);
    }


    private InvoiceResponse convertToResponse(
            Invoice invoice) {

        return new InvoiceResponse(

                invoice.getInvoiceId(),

                invoice.getInvoiceNumber(),

                invoice.getBookingId(),

                invoice.getPaymentId(),

                invoice.getCustomerId(),

                invoice.getCustomerName(),

                invoice.getCustomerEmail(),

                invoice.getFlightNumber(),

                invoice.getSource(),

                invoice.getDestination(),

                invoice.getTravelDate(),

                invoice.getBaseFare(),

                invoice.getTaxAmount(),

                invoice.getServiceFee(),

                invoice.getDiscount(),

                invoice.getTotalAmount(),

                invoice.getCurrency(),

                invoice.getTransactionId(),

                invoice.getPaymentMethod(),

                invoice.getPaymentStatus(),

                invoice.getInvoiceStatus(),

                invoice.getCreatedAt(),

                invoice.getUpdatedAt()
        );
    }


    private String generateInvoiceId() {

        return "INV-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }


    private String generateInvoiceNumber() {

        long number =
                INVOICE_SEQUENCE.getAndIncrement();

        return String.format(
                "INV-2026-%06d",
                number
        );
    }
}