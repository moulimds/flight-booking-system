package com.flight.booking.messaging;

import com.flight.booking.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventListener.class);

    @Autowired
    private BookingService bookingService;

    @RabbitListener(queues = "booking.events.queue")
    public void handlePaymentSuccess(PaymentSuccessfulEvent event) {
        logger.info("Received PaymentSuccessfulEvent for bookingId: {}, paymentId: {}",
                event.getBookingId(), event.getPaymentId());
        try {
            bookingService.handlePaymentSuccess(event.getBookingId(), event.getPaymentId());
        } catch (Exception e) {
            logger.error("Error processing PaymentSuccessfulEvent: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "booking.events.queue")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        logger.info("Received PaymentFailedEvent for bookingId: {}, reason: {}",
                event.getBookingId(), event.getReason());
        try {
            bookingService.handlePaymentFailure(event.getBookingId(), event.getReason());
        } catch (Exception e) {
            logger.error("Error processing PaymentFailedEvent: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "booking.events.queue")
    public void handleCheckInCompleted(CheckInCompletedEvent event) {
        logger.info("Received CheckInCompletedEvent for bookingId: {}", event.getBookingId());
        try {
            bookingService.handleCheckInCompleted(event.getBookingId());
        } catch (Exception e) {
            logger.error("Error processing CheckInCompletedEvent: {}", e.getMessage());
        }
    }
}
