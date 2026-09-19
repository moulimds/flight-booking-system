package com.flighttracking.service;

import com.flighttracking.dto.*;
import com.flighttracking.entity.Flight;
import com.flighttracking.entity.FlightLeg;
import com.flighttracking.entity.FlightStatus;
import com.flighttracking.entity.FlightTracking;
import com.flighttracking.exception.FlightNotFoundException;
import com.flighttracking.exception.InvalidFlightException;
import com.flighttracking.repository.FlightLegRepository;
import com.flighttracking.repository.FlightRepository;
import com.flighttracking.repository.FlightTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightTrackingServiceImpl implements FlightTrackingService {

    private final FlightRepository flightRepository;
    private final FlightLegRepository flightLegRepository;
    private final FlightTrackingRepository flightTrackingRepository;
    private final RestTemplate restTemplate;

    @Value("${flight.service.url:http://localhost:8082/api/flights}")
    private String flightServiceUrl;

    @Override
    @Transactional
    public FlightTrackingResponse startTracking(String flightNumber) {
        Flight flight = findOrCreateFlightFromService(flightNumber);

        if (flight.getOverallStatus() == FlightStatus.LANDED || flight.getOverallStatus() == FlightStatus.CANCELLED) {
            throw new InvalidFlightException("Cannot start tracking for flight '" + flightNumber + "' with status " + flight.getOverallStatus());
        }

        // Find current active leg (first leg not LANDED)
        FlightLeg activeLeg = flight.getLegs().stream()
                .filter(leg -> leg.getStatus() != FlightStatus.LANDED && leg.getStatus() != FlightStatus.CANCELLED)
                .min(Comparator.comparingInt(FlightLeg::getLegNumber))
                .orElse(flight.getLegs().get(0));

        // Initialize leg status if SCHEDULED
        if (activeLeg.getStatus() == FlightStatus.SCHEDULED) {
            activeLeg.setStatus(FlightStatus.BOARDING);
            flightLegRepository.save(activeLeg);
        }

        if (flight.getOverallStatus() == FlightStatus.SCHEDULED) {
            flight.setOverallStatus(activeLeg.getStatus());
            flightRepository.save(flight);
        }

        // Fetch or initialize tracking position
        Optional<FlightTracking> existingTracking = flightTrackingRepository.findByFlightLegId(activeLeg.getId());
        FlightTracking tracking;
        if (existingTracking.isPresent()) {
            tracking = existingTracking.get();
        } else {
            tracking = FlightTracking.builder()
                    .flightLegId(activeLeg.getId())
                    .latitude(13.0827) // Default initial source coordinate
                    .longitude(80.2707)
                    .altitude(0.0)
                    .speed(0.0)
                    .heading(0.0)
                    .distanceRemaining(0.0)
                    .updatedAt(LocalDateTime.now())
                    .build();
            tracking = flightTrackingRepository.save(tracking);
        }

        return buildTrackingResponse(flight, activeLeg, tracking);
    }

    @Override
    @Transactional
    public FlightTrackingResponse stopTracking(String flightNumber) {
        Flight flight = findOrCreateFlightFromService(flightNumber);
        FlightLeg activeLeg = getActiveLeg(flight);
        FlightTracking tracking = flightTrackingRepository.findByFlightLegId(activeLeg.getId())
                .orElseGet(() -> buildDefaultTracking(activeLeg.getId()));

        return buildTrackingResponse(flight, activeLeg, tracking);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightTrackingResponse getCurrentTracking(String flightNumber) {
        Flight flight = findOrCreateFlightFromService(flightNumber);
        FlightLeg activeLeg = getActiveLeg(flight);
        FlightTracking tracking = flightTrackingRepository.findByFlightLegId(activeLeg.getId())
                .orElseGet(() -> buildDefaultTracking(activeLeg.getId()));

        return buildTrackingResponse(flight, activeLeg, tracking);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightPositionResponse getCurrentPosition(String flightNumber) {
        Flight flight = findOrCreateFlightFromService(flightNumber);
        FlightLeg activeLeg = getActiveLeg(flight);
        FlightTracking tracking = flightTrackingRepository.findByFlightLegId(activeLeg.getId())
                .orElseGet(() -> buildDefaultTracking(activeLeg.getId()));

        return FlightPositionResponse.builder()
                .flightNumber(flight.getFlightNumber())
                .legNumber(activeLeg.getLegNumber())
                .latitude(tracking.getLatitude())
                .longitude(tracking.getLongitude())
                .altitude(tracking.getAltitude())
                .speed(tracking.getSpeed())
                .heading(tracking.getHeading())
                .distanceRemaining(tracking.getDistanceRemaining())
                .status(activeLeg.getStatus())
                .updatedAt(tracking.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FlightJourneyResponse getCompleteJourney(String flightNumber) {
        Flight flight = findOrCreateFlightFromService(flightNumber);
        FlightLeg activeLeg = getActiveLeg(flight);

        List<FlightLegResponse> legResponses = flight.getLegs().stream()
                .map(leg -> FlightLegResponse.builder()
                        .id(leg.getId())
                        .legNumber(leg.getLegNumber())
                        .source(leg.getSource())
                        .destination(leg.getDestination())
                        .scheduledDepartureTime(leg.getScheduledDepartureTime())
                        .scheduledArrivalTime(leg.getScheduledArrivalTime())
                        .actualDepartureTime(leg.getActualDepartureTime())
                        .estimatedArrivalTime(leg.getEstimatedArrivalTime())
                        .actualArrivalTime(leg.getActualArrivalTime())
                        .status(leg.getStatus())
                        .build())
                .collect(Collectors.toList());

        return FlightJourneyResponse.builder()
                .flightNumber(flight.getFlightNumber())
                .createdAt(flight.getCreatedAt())
                .overallStatus(flight.getOverallStatus())
                .currentLeg(activeLeg.getLegNumber())
                .legs(legResponses)
                .build();
    }

    private Flight findOrCreateFlightFromService(String flightNumber) {
        // 1. Check local tracking database first
        Optional<Flight> localFlight = flightRepository.findByFlightNumber(flightNumber);
        if (localFlight.isPresent()) {
            return localFlight.get();
        }

        // 2. Try fetching flight inputs from external flight-service (port 8082)
        try {
            String url = flightServiceUrl + "/number/" + flightNumber;
            log.info("Fetching flight input details from flight-service: {}", url);
            ExternalFlightResponse extFlight = restTemplate.getForObject(url, ExternalFlightResponse.class);

            if (extFlight != null && extFlight.getSource() != null && extFlight.getDestination() != null) {
                log.info("Successfully fetched flight inputs for {}: {} -> {}", flightNumber, extFlight.getSource(), extFlight.getDestination());
                return createFlightFromExternalInput(extFlight);
            }
        } catch (Exception e) {
            log.warn("Could not fetch flight {} from flight-service ({}): {}", flightNumber, flightServiceUrl, e.getMessage());
        }

        // 3. Fallback: Throw exception if flight does not exist
        throw new FlightNotFoundException("Flight '" + flightNumber + "' was not found in tracking service or flight-service");
    }

    private Flight createFlightFromExternalInput(ExternalFlightResponse ext) {
        LocalDate depDate = ext.getDepartureDate() != null ? ext.getDepartureDate() : LocalDate.now();
        LocalTime depTime = ext.getDepartureTime() != null ? ext.getDepartureTime() : LocalTime.of(10, 0);
        LocalTime arrTime = ext.getArrivalTime() != null ? ext.getArrivalTime() : depTime.plusHours(2);

        LocalDateTime scheduledDep = LocalDateTime.of(depDate, depTime);
        LocalDateTime scheduledArr = LocalDateTime.of(depDate, arrTime);

        Flight flight = Flight.builder()
                .flightNumber(ext.getFlightNumber())
                .createdAt(LocalDateTime.now())
                .overallStatus(FlightStatus.SCHEDULED)
                .legs(new ArrayList<>())
                .build();

        FlightLeg leg = FlightLeg.builder()
                .legNumber(1)
                .source(ext.getSource().trim())
                .destination(ext.getDestination().trim())
                .scheduledDepartureTime(scheduledDep)
                .scheduledArrivalTime(scheduledArr)
                .status(FlightStatus.SCHEDULED)
                .build();

        flight.addLeg(leg);
        return flightRepository.save(flight);
    }

    private FlightLeg getActiveLeg(Flight flight) {
        return flight.getLegs().stream()
                .filter(leg -> leg.getStatus() != FlightStatus.LANDED && leg.getStatus() != FlightStatus.CANCELLED)
                .min(Comparator.comparingInt(FlightLeg::getLegNumber))
                .orElse(flight.getLegs().get(flight.getLegs().size() - 1));
    }

    private FlightTracking buildDefaultTracking(Long legId) {
        return FlightTracking.builder()
                .flightLegId(legId)
                .latitude(0.0)
                .longitude(0.0)
                .altitude(0.0)
                .speed(0.0)
                .heading(0.0)
                .distanceRemaining(0.0)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private FlightTrackingResponse buildTrackingResponse(Flight flight, FlightLeg leg, FlightTracking tracking) {
        return FlightTrackingResponse.builder()
                .flightNumber(flight.getFlightNumber())
                .createdAt(flight.getCreatedAt())
                .overallStatus(flight.getOverallStatus())
                .currentLeg(leg.getLegNumber())
                .source(leg.getSource())
                .destination(leg.getDestination())
                .latitude(tracking.getLatitude())
                .longitude(tracking.getLongitude())
                .altitude(tracking.getAltitude())
                .speed(tracking.getSpeed())
                .heading(tracking.getHeading())
                .scheduledDeparture(leg.getScheduledDepartureTime())
                .actualDeparture(leg.getActualDepartureTime())
                .scheduledArrival(leg.getScheduledArrivalTime())
                .estimatedArrival(leg.getEstimatedArrivalTime())
                .actualArrival(leg.getActualArrivalTime())
                .updatedAt(tracking.getUpdatedAt())
                .build();
    }
}
