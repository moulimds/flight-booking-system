package com.flight.seat.messaging;

import com.flight.seat.service.SeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SeatEventListener {

    private static final Logger logger = LoggerFactory.getLogger(SeatEventListener.class);

    @Autowired
    private SeatService seatService;

    @RabbitListener(queues = "seat.events.queue")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        logger.info("Received BookingCancelledEvent for bookingId: {}, releasing seat: {}",
                event.getBookingId(), event.getSeatNumber());
        try {
            seatService.releaseSeatByBookingId(event.getBookingId());
        } catch (Exception e) {
            logger.error("Error releasing seat for cancelled booking: {}", e.getMessage());
        }
    }
}
