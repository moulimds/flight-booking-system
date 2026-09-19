package com.flight.invoice.controller;

import com.flight.invoice.dto.InvoiceRequest;
import com.flight.invoice.dto.InvoiceResponse;
import com.flight.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        logger.info("REST request to create invoice for booking id: {}", request.getBookingId());
        InvoiceResponse response = invoiceService.createInvoice(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        logger.info("REST request to get all invoices");
        List<InvoiceResponse> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable("id") Long id) {
        logger.info("REST request to get invoice id: {}", id);
        InvoiceResponse invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<InvoiceResponse> getInvoiceByBookingId(@PathVariable("bookingId") Long bookingId) {
        logger.info("REST request to get invoice by booking id: {}", bookingId);
        InvoiceResponse invoice = invoiceService.getInvoiceByBookingId(bookingId);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByUserId(@PathVariable("userId") Long userId) {
        logger.info("REST request to get invoices for user id: {}", userId);
        List<InvoiceResponse> invoices = invoiceService.getInvoicesByUserId(userId);
        return ResponseEntity.ok(invoices);
    }

    @RequestMapping(value = "/{id}/cancel", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<InvoiceResponse> cancelInvoice(@PathVariable("id") Long id) {
        logger.info("REST request to cancel invoice id: {}", id);
        InvoiceResponse response = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(response);
    }
}
