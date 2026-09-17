package com.example.invoice.controller;

import com.example.invoice.dto.InvoiceRequest;
import com.example.invoice.dto.InvoiceResponse;
import com.example.invoice.dto.InvoiceStatusRequest;
import com.example.invoice.pdf.InvoicePdfService;
import com.example.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    private final InvoicePdfService invoicePdfService;


    // =====================================================
    // HEALTH
    // =====================================================

    @GetMapping("/health")
    public ResponseEntity<String> health() {

        return ResponseEntity.ok(
                invoiceService.health()
        );
    }


    // =====================================================
    // CREATE INVOICE
    // =====================================================

    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody InvoiceRequest request) {

        return ResponseEntity
                .status(201)
                .body(
                        invoiceService.createInvoice(request)
                );
    }


    // =====================================================
    // GET INVOICE BY ID
    // =====================================================

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getInvoice(
            @PathVariable String invoiceId) {

        return ResponseEntity.ok(
                invoiceService.getInvoiceById(invoiceId)
        );
    }


    // =====================================================
    // GET INVOICES BY BOOKING
    // =====================================================

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<InvoiceResponse>>
    getInvoicesByBooking(
            @PathVariable String bookingId) {

        return ResponseEntity.ok(
                invoiceService
                        .getInvoicesByBooking(bookingId)
        );
    }


    // =====================================================
    // GET INVOICES BY CUSTOMER
    // =====================================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<InvoiceResponse>>
    getInvoicesByCustomer(
            @PathVariable String customerId) {

        return ResponseEntity.ok(
                invoiceService
                        .getInvoicesByCustomer(customerId)
        );
    }


    // =====================================================
    // UPDATE STATUS
    // =====================================================

    @PatchMapping("/{invoiceId}/status")
    public ResponseEntity<InvoiceResponse>
    updateStatus(

            @PathVariable String invoiceId,

            @Valid
            @RequestBody InvoiceStatusRequest request) {

        return ResponseEntity.ok(

                invoiceService.updateStatus(
                        invoiceId,
                        request.status()
                )
        );
    }


    // =====================================================
    // PRINT INVOICE
    // =====================================================

    @GetMapping("/{invoiceId}/print")
    public ResponseEntity<InvoiceResponse>
    printInvoice(
            @PathVariable String invoiceId) {

        return ResponseEntity.ok(
                invoiceService.getInvoiceById(invoiceId)
        );
    }


    // =====================================================
    // DOWNLOAD / VIEW PDF
    // =====================================================

    @GetMapping(
            value = "/{invoiceId}/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> generatePdf(
            @PathVariable String invoiceId) {

        InvoiceResponse invoice =
                invoiceService.getInvoiceById(invoiceId);


        byte[] pdf =
                invoicePdfService.generatePdf(invoice);


        HttpHeaders headers =
                new HttpHeaders();


        headers.setContentType(
                MediaType.APPLICATION_PDF
        );


        headers.setContentDisposition(

                ContentDisposition
                        .inline()
                        .filename(
                                invoice.invoiceNumber()
                                        + ".pdf"
                        )
                        .build()
        );


        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}