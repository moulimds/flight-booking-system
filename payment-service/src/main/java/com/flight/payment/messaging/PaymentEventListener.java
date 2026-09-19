package com.flight.payment.messaging;

import com.flight.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListener.class);

    @Autowired
    private PaymentService paymentService;

    @RabbitListener(queues = "payment.events.queue")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        logger.info("Received BookingCancelledEvent for bookingId: {}, refunding payment...", event.getBookingId());
        try {
            paymentService.handleBookingCancelled(event.getBookingId(), event.getRefundAmount());
        } catch (Exception e) {
            logger.error("Error processing refund for cancelled booking: {}", e.getMessage());
        }
    }
}
