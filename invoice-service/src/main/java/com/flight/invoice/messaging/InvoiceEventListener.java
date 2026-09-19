package com.flight.invoice.messaging;

import com.flight.invoice.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEventListener {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceEventListener.class);

    @Autowired
    private InvoiceService invoiceService;

    @RabbitListener(queues = "invoice.events.queue")
    public void handlePaymentSuccess(PaymentSuccessfulEvent event) {
        logger.info("Received PaymentSuccessfulEvent for bookingId: {}, generating invoice...", event.getBookingId());
        try {
            invoiceService.handlePaymentSuccess(
                    event.getPaymentId(),
                    event.getBookingId(),
                    event.getUserId(),
                    event.getAmount()
            );
        } catch (Exception e) {
            logger.error("Error generating invoice from PaymentSuccessfulEvent: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "invoice.events.queue")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        logger.info("Received BookingCancelledEvent for bookingId: {}, cancelling invoice...", event.getBookingId());
        try {
            invoiceService.handleBookingCancelled(event.getBookingId());
        } catch (Exception e) {
            logger.error("Error cancelling invoice from BookingCancelledEvent: {}", e.getMessage());
        }
    }
}
