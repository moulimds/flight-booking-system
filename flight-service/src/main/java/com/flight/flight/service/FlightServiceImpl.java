package com.flight.flight.service;

import com.flight.flight.dto.FlightRequest;
import com.flight.flight.dto.FlightResponse;
import com.flight.flight.dto.FlightSeatsUpdateRequest;
import com.flight.flight.dto.FlightStatusUpdateRequest;
import com.flight.flight.entity.Flight;
import com.flight.flight.exception.DuplicateFlightException;
import com.flight.flight.exception.InvalidFlightException;
import com.flight.flight.exception.ResourceNotFoundException;
import com.flight.flight.messaging.FlightCreatedEvent;
import com.flight.flight.messaging.FlightDeletedEvent;
import com.flight.flight.messaging.FlightEventPublisher;
import com.flight.flight.messaging.FlightUpdatedEvent;
import com.flight.flight.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FlightServiceImpl implements FlightService {

    private static final Logger logger = LoggerFactory.getLogger(FlightServiceImpl.class);

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightEventPublisher flightEventPublisher;

    @Override
    public FlightResponse createFlight(FlightRequest request) {
        logger.info("Creating flight: {}", request.getFlightNumber());

        validateFlightRequest(request);

        if (flightRepository.existsByFlightNumber(request.getFlightNumber())) {
            throw new DuplicateFlightException("Flight with number '" + request.getFlightNumber() + "' already exists");
        }

        Flight flight = new Flight();
        flight.setFlightNumber(request.getFlightNumber().trim().toUpperCase());
        flight.setAirline(request.getAirline());
        flight.setSource(request.getSource().trim().toUpperCase());
        flight.setDestination(request.getDestination().trim().toUpperCase());
        flight.setDepartureDate(request.getDepartureDate());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(request.getAvailableSeats() != null ? request.getAvailableSeats() : request.getTotalSeats());
        flight.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "SCHEDULED");

        Flight savedFlight = flightRepository.save(flight);

        try {
            flightEventPublisher.publishFlightCreated(new FlightCreatedEvent(
                    savedFlight.getId(),
                    savedFlight.getFlightNumber(),
                    savedFlight.getAirline(),
                    savedFlight.getSource(),
                    savedFlight.getDestination(),
                    savedFlight.getDepartureDate(),
                    savedFlight.getDepartureTime(),
                    savedFlight.getArrivalTime(),
                    savedFlight.getTotalSeats(),
                    savedFlight.getAvailableSeats(),
                    savedFlight.getStatus()
            ));
        } catch (Exception e) {
            logger.warn("Failed to publish FlightCreatedEvent: {}", e.getMessage());
        }

        return mapToFlightResponse(savedFlight);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightResponse> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(this::mapToFlightResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
        return mapToFlightResponse(flight);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlightByFlightNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with flight number: " + flightNumber));
        return mapToFlightResponse(flight);
    }

    @Override
    public FlightResponse updateFlight(Long id, FlightRequest request) {
        logger.info("Updating flight with id: {}", id);

        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));

        validateFlightRequest(request);

        if (!flight.getFlightNumber().equalsIgnoreCase(request.getFlightNumber()) &&
                flightRepository.existsByFlightNumber(request.getFlightNumber())) {
            throw new DuplicateFlightException("Flight with number '" + request.getFlightNumber() + "' already exists");
        }

        flight.setFlightNumber(request.getFlightNumber().trim().toUpperCase());
        flight.setAirline(request.getAirline());
        flight.setSource(request.getSource().trim().toUpperCase());
        flight.setDestination(request.getDestination().trim().toUpperCase());
        flight.setDepartureDate(request.getDepartureDate());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(request.getAvailableSeats() != null ? request.getAvailableSeats() : flight.getAvailableSeats());
        if (request.getStatus() != null) {
            flight.setStatus(request.getStatus().toUpperCase());
        }

        Flight updatedFlight = flightRepository.save(flight)

        try {
            flightEventPublisher.publishFlightUpdated(new FlightUpdatedEvent(
                    updatedFlight.getId(),
                    updatedFlight.getFlightNumber(),
                    updatedFlight.getAirline(),
                    updatedFlight.getSource(),
                    updatedFlight.getDestination(),
                    updatedFlight.getDepartureDate(),
                    updatedFlight.getDepartureTime(),
                    updatedFlight.getArrivalTime(),
                    updatedFlight.getTotalSeats(),
                    updatedFlight.getAvailableSeats(),
                    updatedFlight.getStatus()
            ));
        } catch (Exception e) {
            logger.warn("Failed to publish FlightUpdatedEvent: {}", e.getMessage());
        }

        return mapToFlightResponse(updatedFlight);
    }

    @Override
    public void deleteFlight(Long id) {
        logger.info("Deleting flight with id: {}", id);
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));

        flightRepository.delete(flight);

        try {
            flightEventPublisher.publishFlightDeleted(new FlightDeletedEvent(id, flight.getFlightNumber()));
        } catch (Exception e) {
            logger.warn("Failed to publish FlightDeletedEvent: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightResponse> getFlightsBySource(String source) {
        return flightRepository.findBySourceIgnoreCase(source).stream()
                .map(this::mapToFlightResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightResponse> getFlightsByDestination(String destination) {
        return flightRepository.findByDestinationIgnoreCase(destination).stream()
                .map(this::mapToFlightResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightResponse> searchFlights(String source, String destination, LocalDate departureDate) {
        if (source != null && destination != null && departureDate != null) {
            return flightRepository.findBySourceIgnoreCaseAndDestinationIgnoreCaseAndDepartureDate(source, destination, departureDate)
                    .stream().map(this::mapToFlightResponse).collect(Collectors.toList());
        } else if (source != null && destination != null) {
            return flightRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(source, destination)
                    .stream().map(this::mapToFlightResponse).collect(Collectors.toList());
        } else if (source != null) {
            return getFlightsBySource(source);
        } else if (destination != null) {
            return getFlightsByDestination(destination);
        }
        return getAllFlights()
    }

    @Override
    public FlightResponse updateFlightStatus(Long id, FlightStatusUpdateRequest request) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));

        flight.setStatus(request.getStatus().toUpperCase());
        Flight updatedFlight = flightRepository.save(flight);

        try {
            flightEventPublisher.publishFlightUpdated(new FlightUpdatedEvent(
                    updatedFlight.getId(),
                    updatedFlight.getFlightNumber(),
                    updatedFlight.getAirline(),
                    updatedFlight.getSource(),
                    updatedFlight.getDestination(),
                    updatedFlight.getDepartureDate(),
                    updatedFlight.getDepartureTime(),
                    updatedFlight.getArrivalTime(),
                    updatedFlight.getTotalSeats(),
                    updatedFlight.getAvailableSeats(),
                    updatedFlight.getStatus()
            ));
        } catch (Exception e) {
            logger.warn("Failed to publish FlightUpdatedEvent: {}", e.getMessage());
        }

        return mapToFlightResponse(updatedFlight);
    }

    @Override
    public FlightResponse updateAvailableSeats(Long id, FlightSeatsUpdateRequest request) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));

        if (request.getAvailableSeats() < 0 || request.getAvailableSeats() > flight.getTotalSeats()) {
            throw new InvalidFlightException("Available seats must be between 0 and total seats (" + flight.getTotalSeats() + ")");
        }

        flight.setAvailableSeats(request.getAvailableSeats());
        Flight updatedFlight = flightRepository.save(flight);

        try {
            flightEventPublisher.publishFlightUpdated(new FlightUpdatedEvent(
                    updatedFlight.getId(),
                    updatedFlight.getFlightNumber(),
                    updatedFlight.getAirline(),
                    updatedFlight.getSource(),
                    updatedFlight.getDestination(),
                    updatedFlight.getDepartureDate(),
                    updatedFlight.getDepartureTime(),
                    updatedFlight.getArrivalTime(),
                    updatedFlight.getTotalSeats(),
                    updatedFlight.getAvailableSeats(),
                    updatedFlight.getStatus()
            ));
        } catch (Exception e) {
            logger.warn("Failed to publish FlightUpdatedEvent: {}", e.getMessage());
        }

        return mapToFlightResponse(updatedFlight);
    }

    private void validateFlightRequest(FlightRequest request) {
        if (request.getSource().trim().equalsIgnoreCase(request.getDestination().trim())) {
            throw new InvalidFlightException("Flight source and destination cannot be the same");
        }
        if (request.getTotalSeats() <= 0) {
            throw new InvalidFlightException("Total seats must be greater than zero");
        }
        if (request.getAvailableSeats() != null && request.getAvailableSeats() > request.getTotalSeats()) {
            throw new InvalidFlightException("Available seats cannot exceed total seats");
        }
    }

    private FlightResponse mapToFlightResponse(Flight flight) {
        return new FlightResponse(
                flight.getId(),
                flight.getFlightNumber(),
                flight.getAirline(),
                flight.getSource(),
                flight.getDestination(),
                flight.getDepartureDate(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getTotalSeats(),
                flight.getAvailableSeats(),
                flight.getStatus(),
                flight.getCreatedAt(),
                flight.getUpdatedAt()
        );
    }
}
