package com.example.payment.service;

import com.example.payment.dto.RefundRequest;
import com.example.payment.dto.RefundResponse;

public interface RefundService {

    RefundResponse initiateRefund(RefundRequest request);

    RefundResponse getRefund(String refundId);

    RefundResponse processRefund(String refundId);
}