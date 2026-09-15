package com.example.invoice.messaging;

import com.example.invoice.dto.InvoiceRequest;
import com.example.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final InvoiceService invoiceService;


    @RabbitListener(
            queues = RabbitMQConfig.PAYMENT_SUCCESS_QUEUE
    )
    public void handlePaymentSuccess(
            PaymentSuccessEvent event) {

        System.out.println(
                "Received PAYMENT_SUCCESS event: "
                        + event
        );


        InvoiceRequest request =
                new InvoiceRequest(

                        event.bookingId(),

                        event.paymentId(),

                        event.customerId(),

                        event.customerName(),

                        event.customerEmail(),

                        event.flightNumber(),

                        event.source(),

                        event.destination(),

                        event.travelDate(),

                        event.baseFare(),

                        event.taxAmount(),

                        event.serviceFee(),

                        event.discount(),

                        event.amount(),

                        event.currency(),

                        event.transactionId(),

                        event.paymentMethod(),

                        "SUCCESS"
                );


        invoiceService.createInvoice(request);


        System.out.println(
                "Invoice created successfully for booking: "
                        + event.bookingId()
        );
    }


    public record PaymentSuccessEvent(

            String paymentId,

            String bookingId,

            String customerId,

            String customerName,

            String customerEmail,

            String flightNumber,

            String source,

            String destination,

            LocalDate travelDate,

            BigDecimal baseFare,

            BigDecimal taxAmount,

            BigDecimal serviceFee,

            BigDecimal discount,

            BigDecimal amount,

            String currency,

            String transactionId,

            String paymentMethod

    ) {
    }
}