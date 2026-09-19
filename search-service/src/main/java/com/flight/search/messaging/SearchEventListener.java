package com.flight.search.messaging;

import com.flight.search.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SearchEventListener {

    private static final Logger logger = LoggerFactory.getLogger(SearchEventListener.class);

    @Autowired
    private SearchService searchService;

    @RabbitListener(queues = "search.events.queue")
    public void handleFlightCreated(FlightCreatedEvent event) {
        logger.info("Received FlightCreatedEvent for flightId: {}", event.getFlightId());
        try {
            searchService.updateOrIndexFlight(event.getFlightId());
        } catch (Exception e) {
            logger.error("Error processing FlightCreatedEvent: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "search.events.queue")
    public void handleFlightUpdated(FlightUpdatedEvent event) {
        logger.info("Received FlightUpdatedEvent for flightId: {}", event.getFlightId());
        try {
            searchService.updateOrIndexFlight(event.getFlightId());
        } catch (Exception e) {
            logger.error("Error processing FlightUpdatedEvent: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "search.events.queue")
    public void handleFlightDeleted(FlightDeletedEvent event) {
        logger.info("Received FlightDeletedEvent for flightId: {}", event.getFlightId());
        try {
            searchService.removeFlightFromIndex(event.getFlightId());
        } catch (Exception e) {
            logger.error("Error processing FlightDeletedEvent: {}", e.getMessage());
        }
    }
}
