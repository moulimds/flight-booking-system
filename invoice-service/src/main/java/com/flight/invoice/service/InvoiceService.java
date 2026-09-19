package com.flight.invoice.service;

import com.flight.invoice.dto.InvoiceRequest;
import com.flight.invoice.dto.InvoiceResponse;

import java.util.List;

public interface InvoiceService {
    InvoiceResponse createInvoice(InvoiceRequest request);
    List<InvoiceResponse> getAllInvoices();
    InvoiceResponse getInvoiceById(Long id);
    InvoiceResponse getInvoiceByBookingId(Long bookingId);
    List<InvoiceResponse> getInvoicesByUserId(Long userId);
    InvoiceResponse cancelInvoice(Long id);
    void handlePaymentSuccess(Long paymentId, Long bookingId, Long userId, java.math.BigDecimal amount);
    void handleBookingCancelled(Long bookingId);
}
