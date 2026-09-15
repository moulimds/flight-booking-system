package com.flight.search.service;

import com.flight.search.client.FareClient;
import com.flight.search.client.FlightClient;
import com.flight.search.dto.FareDto;
import com.flight.search.dto.FlightDto;
import com.flight.search.dto.FlightSearchResult;
import com.flight.search.entity.FlightSearchIndex;
import com.flight.search.repository.FlightSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SearchServiceImpl implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Autowired
    private FlightSearchRepository flightSearchRepository;

    @Autowired
    private FlightClient flightClient;

    @Autowired
    private FareClient fareClient;

    @Override
    @Transactional(readOnly = true)
    public List<FlightSearchResult> searchFlights(String source, String destination, LocalDate departureDate) {
        logger.info("Searching flights with criteria: source={}, destination={}, departureDate={}", source, destination, departureDate);

        // If index is empty, attempt initial sync
        if (flightSearchRepository.count() == 0) {
            try {
                syncFlightData();
            } catch (Exception e) {
                logger.warn("Initial sync failed: {}", e.getMessage());
            }
        }

        List<FlightSearchIndex> indices;

        if (source != null && destination != null && departureDate != null) {
            indices = flightSearchRepository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndDepartureDate(
                    source.trim(), destination.trim(), departureDate);
        } else if (source != null && destination != null) {
            indices = flightSearchRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(
                    source.trim(), destination.trim());
        } else if (source != null) {
            indices = flightSearchRepository.findBySourceIgnoreCase(source.trim());
        } else if (destination != null) {
            indices = flightSearchRepository.findByDestinationIgnoreCase(destination.trim());
        } else {
            indices = flightSearchRepository.findAll();
        }

        return indices.stream()
                .filter(idx -> !"CANCELLED".equalsIgnoreCase(idx.getStatus()))
                .map(this::mapToSearchResult)
                .collect(Collectors.toList());
    }

    @Override
    public void syncFlightData() {
        logger.info("Synchronizing flight search catalog from Flight and Fare services...");
        try {
            List<FlightDto> flights = flightClient.getAllFlights();
            if (flights != null) {
                for (FlightDto flight : flights) {
                    BigDecimal fareAmount = BigDecimal.ZERO;
                    try {
                        FareDto fare = fareClient.getFareByFlightId(flight.getId());
                        if (fare != null && fare.getFinalFare() != null) {
                            fareAmount = fare.getFinalFare();
                        }
                    } catch (Exception e) {
                        logger.warn("Could not fetch fare for flightId {}: {}", flight.getId(), e.getMessage());
                    }

                    Optional<FlightSearchIndex> existing = flightSearchRepository.findByFlightId(flight.getId());
                    FlightSearchIndex index = existing.orElse(new FlightSearchIndex());
                    index.setFlightId(flight.getId());
                    index.setFlightNumber(flight.getFlightNumber());
                    index.setAirline(flight.getAirline());
                    index.setSource(flight.getSource());
                    index.setDestination(flight.getDestination());
                    index.setDepartureDate(flight.getDepartureDate());
                    index.setDepartureTime(flight.getDepartureTime());
                    index.setArrivalTime(flight.getArrivalTime());
                    index.setAvailableSeats(flight.getAvailableSeats());
                    index.setFare(fareAmount);
                    index.setStatus(flight.getStatus());

                    flightSearchRepository.save(index);
                }
            }
            logger.info("Successfully synced flight search index.");
        } catch (Exception e) {
            logger.error("Failed to sync flights: {}", e.getMessage());
        }
    }

    @Override
    public void updateOrIndexFlight(Long flightId) {
        try {
            FlightDto flight = flightClient.getFlightById(flightId);
            if (flight != null) {
                BigDecimal fareAmount = BigDecimal.ZERO;
                try {
                    FareDto fare = fareClient.getFareByFlightId(flight.getId());
                    if (fare != null && fare.getFinalFare() != null) {
                        fareAmount = fare.getFinalFare();
                    }
                } catch (Exception e) {
                    logger.warn("Could not fetch fare for flightId {}: {}", flight.getId(), e.getMessage());
                }

                Optional<FlightSearchIndex> existing = flightSearchRepository.findByFlightId(flight.getId());
                FlightSearchIndex index = existing.orElse(new FlightSearchIndex());
                index.setFlightId(flight.getId());
                index.setFlightNumber(flight.getFlightNumber());
                index.setAirline(flight.getAirline());
                index.setSource(flight.getSource());
                index.setDestination(flight.getDestination());
                index.setDepartureDate(flight.getDepartureDate());
                index.setDepartureTime(flight.getDepartureTime());
                index.setArrivalTime(flight.getArrivalTime());
                index.setAvailableSeats(flight.getAvailableSeats());
                index.setFare(fareAmount);
                index.setStatus(flight.getStatus());

                flightSearchRepository.save(index);
            }
        } catch (Exception e) {
            logger.error("Failed to update index for flightId {}: {}", flightId, e.getMessage());
        }
    }

    @Override
    public void removeFlightFromIndex(Long flightId) {
        flightSearchRepository.deleteByFlightId(flightId);
    }

    private FlightSearchResult mapToSearchResult(FlightSearchIndex index) {
        return new FlightSearchResult(
                index.getFlightId(),
                index.getFlightNumber(),
                index.getAirline(),
                index.getSource(),
                index.getDestination(),
                index.getDepartureDate(),
                index.getDepartureTime(),
                index.getArrivalTime(),
                index.getAvailableSeats(),
                index.getFare(),
                index.getStatus()
        );
    }
}
