package com.flighttracking.service;

import com.flighttracking.dto.CreateFlightLegRequest;
import com.flighttracking.dto.CreateFlightRequest;
import com.flighttracking.dto.FlightLegResponse;
import com.flighttracking.dto.FlightResponse;
import com.flighttracking.entity.Flight;
import com.flighttracking.entity.FlightLeg;
import com.flighttracking.entity.FlightStatus;
import com.flighttracking.exception.FlightNotFoundException;
import com.flighttracking.exception.InvalidFlightException;
import com.flighttracking.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;

    @Override
    @Transactional
    public FlightResponse createFlight(CreateFlightRequest request) {
        if (request.getFlightNumber() == null || request.getFlightNumber().trim().isEmpty()) {
            throw new InvalidFlightException("Flight number is required");
        }

        String flightNum = request.getFlightNumber().trim();

        if (flightRepository.existsByFlightNumber(flightNum)) {
            throw new InvalidFlightException("Flight number '" + flightNum + "' already exists");
        }

        if (request.getLegs() == null || request.getLegs().isEmpty()) {
            throw new InvalidFlightException("Flight must have at least one flight leg");
        }

        Set<Integer> legNumbers = new HashSet<>();
        Flight flight = Flight.builder()
                .flightNumber(flightNum)
                .createdAt(LocalDateTime.now())
                .overallStatus(FlightStatus.SCHEDULED)
                .legs(new ArrayList<>())
                .build();

        int autoSequence = 1;
        for (CreateFlightLegRequest legReq : request.getLegs()) {
            int legNum = legReq.getLegNumber() != null ? legReq.getLegNumber() : autoSequence;

            if (!legNumbers.add(legNum)) {
                throw new InvalidFlightException("Duplicate leg number " + legNum + " found in flight " + flightNum);
            }

            if (legReq.getSource() == null || legReq.getSource().trim().isEmpty()) {
                throw new InvalidFlightException("Leg " + legNum + ": Source airport is required");
            }

            if (legReq.getDestination() == null || legReq.getDestination().trim().isEmpty()) {
                throw new InvalidFlightException("Leg " + legNum + ": Destination airport is required");
            }

            String src = legReq.getSource().trim();
            String dest = legReq.getDestination().trim();

            if (src.equalsIgnoreCase(dest)) {
                throw new InvalidFlightException("Leg " + legNum + ": Source and destination cannot be identical ('" + src + "')");
            }

            if (legReq.getScheduledDepartureTime() == null) {
                throw new InvalidFlightException("Leg " + legNum + ": Scheduled departure time is required");
            }

            if (legReq.getScheduledArrivalTime() == null) {
                throw new InvalidFlightException("Leg " + legNum + ": Scheduled arrival time is required");
            }

            if (legReq.getScheduledDepartureTime().isAfter(legReq.getScheduledArrivalTime()) ||
                legReq.getScheduledDepartureTime().isEqual(legReq.getScheduledArrivalTime())) {
                throw new InvalidFlightException("Leg " + legNum + ": Scheduled departure time must be strictly before scheduled arrival time");
            }

            FlightLeg leg = FlightLeg.builder()
                    .legNumber(legNum)
                    .source(src)
                    .destination(dest)
                    .scheduledDepartureTime(legReq.getScheduledDepartureTime())
                    .scheduledArrivalTime(legReq.getScheduledArrivalTime())
                    .status(FlightStatus.SCHEDULED)
                    .build();

            flight.addLeg(leg);
            autoSequence++;
        }

        Flight saved = flightRepository.save(flight);
        return mapToFlightResponse(saved);
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
    public FlightResponse getFlightByFlightNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new FlightNotFoundException("Flight '" + flightNumber + "' was not found"));
        return mapToFlightResponse(flight);
    }

    private FlightResponse mapToFlightResponse(Flight flight) {
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

        return FlightResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .createdAt(flight.getCreatedAt())
                .overallStatus(flight.getOverallStatus())
                .legs(legResponses)
                .build();
    }
}
