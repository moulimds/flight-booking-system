package com.example.invoice.service;

import com.example.invoice.dto.InvoiceRequest;
import com.example.invoice.dto.InvoiceResponse;
import com.example.invoice.enums.InvoiceStatus;

import java.util.List;

public interface InvoiceService {

    String health();

    InvoiceResponse createInvoice(InvoiceRequest request);

    InvoiceResponse getInvoiceById(String invoiceId);

    List<InvoiceResponse> getInvoicesByBooking(String bookingId);

    List<InvoiceResponse> getInvoicesByCustomer(String customerId);

    InvoiceResponse updateStatus(
            String invoiceId,
            InvoiceStatus status
    );

}