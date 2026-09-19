package com.flighttracking.simulator;

import com.flighttracking.airport.AirportCoordinate;
import com.flighttracking.entity.Flight;
import com.flighttracking.entity.FlightLeg;
import com.flighttracking.entity.FlightStatus;
import com.flighttracking.entity.FlightTracking;
import com.flighttracking.provider.FlightDataProvider;
import com.flighttracking.repository.FlightLegRepository;
import com.flighttracking.repository.FlightRepository;
import com.flighttracking.repository.FlightTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlightLocationSimulator {

    private final FlightRepository flightRepository;
    private final FlightLegRepository flightLegRepository;
    private final FlightTrackingRepository flightTrackingRepository;
    private final FlightDataProvider flightDataProvider;

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void simulateFlightMovements() {
        List<FlightStatus> activeStatuses = Arrays.asList(FlightStatus.BOARDING, FlightStatus.DEPARTED, FlightStatus.IN_AIR);
        List<FlightLeg> activeLegs = flightLegRepository.findByStatusIn(activeStatuses);

        if (activeLegs.isEmpty()) {
            return;
        }

        for (FlightLeg leg : activeLegs) {
            Flight flight = leg.getFlight();

            AirportCoordinate sourceCoord = flightDataProvider.getAirportCoordinates(leg.getSource());
            AirportCoordinate destCoord = flightDataProvider.getAirportCoordinates(leg.getDestination());

            // Handle BOARDING / DEPARTED transition
            if (leg.getStatus() == FlightStatus.BOARDING || leg.getStatus() == FlightStatus.DEPARTED) {
                if (leg.getActualDepartureTime() == null) {
                    leg.setActualDepartureTime(LocalDateTime.now());
                }
                leg.setStatus(FlightStatus.IN_AIR);
                flightLegRepository.save(leg);

                flight.setOverallStatus(FlightStatus.IN_AIR);
                flightRepository.save(flight);

                log.info("Flight {} (Leg {}) has DEPARTED from {} and is now IN_AIR",
                        flight.getFlightNumber(), leg.getLegNumber(), leg.getSource());
            }

            // Fetch or create tracking position
            FlightTracking tracking = flightTrackingRepository.findByFlightLegId(leg.getId())
                    .orElseGet(() -> FlightTracking.builder()
                            .flightLegId(leg.getId())
                            .latitude(sourceCoord.getLatitude())
                            .longitude(sourceCoord.getLongitude())
                            .altitude(0.0)
                            .speed(0.0)
                            .heading(0.0)
                            .distanceRemaining(flightDataProvider.calculateDistanceKm(
                                    sourceCoord.getLatitude(), sourceCoord.getLongitude(),
                                    destCoord.getLatitude(), destCoord.getLongitude()))
                            .updatedAt(LocalDateTime.now())
                            .build());

            // Update position
            tracking = flightDataProvider.updatePosition(tracking, sourceCoord, destCoord);

            // Dynamically calculate ETA based on distance remaining and cruising speed
            if (tracking.getSpeed() > 0 && tracking.getDistanceRemaining() > 0) {
                double speedKmH = tracking.getSpeed() * 1.852; // knots to km/h
                double hoursRemaining = tracking.getDistanceRemaining() / speedKmH;
                long minutesRemaining = Math.max(1, Math.round(hoursRemaining * 60));
                leg.setEstimatedArrivalTime(LocalDateTime.now().plusMinutes(minutesRemaining));
            }

            flightTrackingRepository.save(tracking);

            // Destination Detection Threshold (5.0 km)
            if (tracking.getDistanceRemaining() <= 5.0 ||
                    (tracking.getLatitude().equals(destCoord.getLatitude()) && tracking.getLongitude().equals(destCoord.getLongitude()))) {
                
                leg.setStatus(FlightStatus.LANDED);
                leg.setActualArrivalTime(LocalDateTime.now());
                leg.setEstimatedArrivalTime(leg.getActualArrivalTime());
                flightLegRepository.save(leg);

                log.info("Flight {} (Leg {}) has LANDED at {}", flight.getFlightNumber(), leg.getLegNumber(), leg.getDestination());

                // Check overall flight completion across all legs
                updateOverallFlightStatus(flight);
            } else {
                flightLegRepository.save(leg);
            }
        }
    }

    private void updateOverallFlightStatus(Flight flight) {
        List<FlightLeg> allLegs = flightLegRepository.findByFlightIdOrderByLegNumberAsc(flight.getId());
        boolean allLanded = allLegs.stream().allMatch(l -> l.getStatus() == FlightStatus.LANDED);

        if (allLanded) {
            flight.setOverallStatus(FlightStatus.LANDED);
            log.info("Overall Flight {} has completed all legs and is LANDED", flight.getFlightNumber());
        } else {
            // Find next leg to activate
            Optional<FlightLeg> nextLegOpt = allLegs.stream()
                    .filter(l -> l.getStatus() == FlightStatus.SCHEDULED)
                    .min(Comparator.comparingInt(FlightLeg::getLegNumber));

            if (nextLegOpt.isPresent()) {
                FlightLeg nextLeg = nextLegOpt.get();
                nextLeg.setStatus(FlightStatus.BOARDING);
                flightLegRepository.save(nextLeg);
                flight.setOverallStatus(FlightStatus.BOARDING);
                log.info("Flight {} transitioning to next Leg {} ({})", flight.getFlightNumber(), nextLeg.getLegNumber(), nextLeg.getSource());
            }
        }
        flightRepository.save(flight);
    }
}
