package com.example.payment.event;

import com.example.payment.entity.Payment;
import com.example.payment.entity.Refund;

public interface PaymentEventPublisher {

    void publishPaymentSuccess(Payment payment);

    void publishPaymentFailed(Payment payment);

    void publishRefundSuccess(Refund refund);
}