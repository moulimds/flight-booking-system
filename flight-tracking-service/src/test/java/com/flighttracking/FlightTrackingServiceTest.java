package com.flighttracking;

import com.flighttracking.dto.*;
import com.flighttracking.entity.FlightStatus;
import com.flighttracking.exception.FlightNotFoundException;
import com.flighttracking.exception.InvalidFlightException;
import com.flighttracking.service.FlightService;
import com.flighttracking.service.FlightTrackingService;
import com.flighttracking.simulator.FlightLocationSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class FlightTrackingServiceTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightTrackingService flightTrackingService;

    @Autowired
    private FlightLocationSimulator flightLocationSimulator;

    @Test
    @DisplayName("1. Direct Flight Lifecycle Test (Chennai -> Delhi)")
    public void testDirectFlightLifecycle() {
        CreateFlightRequest request = CreateFlightRequest.builder()
                .flightNumber("AI202")
                .legs(Collections.singletonList(
                        CreateFlightLegRequest.builder()
                                .legNumber(1)
                                .source("Chennai")
                                .destination("Delhi")
                                .scheduledDepartureTime(LocalDateTime.now().plusHours(1))
                                .scheduledArrivalTime(LocalDateTime.now().plusHours(3))
                                .build()
                ))
                .build();

        FlightResponse created = flightService.createFlight(request);
        assertThat(created).isNotNull();
        assertThat(created.getOverallStatus()).isEqualTo(FlightStatus.SCHEDULED);
        assertThat(created.getLegs()).hasSize(1);
        assertThat(created.getLegs().get(0).getStatus()).isEqualTo(FlightStatus.SCHEDULED);

        // Start tracking
        FlightTrackingResponse tracking = flightTrackingService.startTracking("AI202");
        assertThat(tracking.getOverallStatus()).isEqualTo(FlightStatus.BOARDING);

        // Simulate flight departure & in-air movement
        flightLocationSimulator.simulateFlightMovements();
        FlightJourneyResponse journeyInAir = flightTrackingService.getCompleteJourney("AI202");
        assertThat(journeyInAir.getOverallStatus()).isEqualTo(FlightStatus.IN_AIR);
        assertThat(journeyInAir.getLegs().get(0).getActualDepartureTime()).isNotNull();

        // Get live position
        FlightPositionResponse position = flightTrackingService.getCurrentPosition("AI202");
        assertThat(position.getLatitude()).isNotNull();
        assertThat(position.getLongitude()).isNotNull();
        assertThat(position.getStatus()).isEqualTo(FlightStatus.IN_AIR);

        // Simulate until landed
        for (int i = 0; i < 25; i++) {
            flightLocationSimulator.simulateFlightMovements();
        }

        FlightJourneyResponse journeyLanded = flightTrackingService.getCompleteJourney("AI202");
        assertThat(journeyLanded.getOverallStatus()).isEqualTo(FlightStatus.LANDED);
        assertThat(journeyLanded.getLegs().get(0).getActualArrivalTime()).isNotNull();
    }

    @Test
    @DisplayName("2. Multi-Way Flight Lifecycle Test (Chennai -> Mumbai -> Delhi -> Kolkata)")
    public void testMultiWayFlightLifecycle() {
        CreateFlightRequest request = CreateFlightRequest.builder()
                .flightNumber("AI500")
                .legs(Arrays.asList(
                        CreateFlightLegRequest.builder()
                                .legNumber(1)
                                .source("Chennai")
                                .destination("Mumbai")
                                .scheduledDepartureTime(LocalDateTime.now().plusHours(1))
                                .scheduledArrivalTime(LocalDateTime.now().plusHours(3))
                                .build(),
                        CreateFlightLegRequest.builder()
                                .legNumber(2)
                                .source("Mumbai")
                                .destination("Delhi")
                                .scheduledDepartureTime(LocalDateTime.now().plusHours(4))
                                .scheduledArrivalTime(LocalDateTime.now().plusHours(6))
                                .build(),
                        CreateFlightLegRequest.builder()
                                .legNumber(3)
                                .source("Delhi")
                                .destination("Kolkata")
                                .scheduledDepartureTime(LocalDateTime.now().plusHours(7))
                                .scheduledArrivalTime(LocalDateTime.now().plusHours(9))
                                .build()
                ))
                .build();

        FlightResponse created = flightService.createFlight(request);
        assertThat(created.getLegs()).hasSize(3);

        flightTrackingService.startTracking("AI500");

        // Simulate until all 3 legs finish
        for (int i = 0; i < 75; i++) {
            flightLocationSimulator.simulateFlightMovements();
        }

        FlightJourneyResponse finalJourney = flightTrackingService.getCompleteJourney("AI500");
        assertThat(finalJourney.getOverallStatus()).isEqualTo(FlightStatus.LANDED);
        assertThat(finalJourney.getLegs().get(0).getStatus()).isEqualTo(FlightStatus.LANDED);
        assertThat(finalJourney.getLegs().get(1).getStatus()).isEqualTo(FlightStatus.LANDED);
        assertThat(finalJourney.getLegs().get(2).getStatus()).isEqualTo(FlightStatus.LANDED);
    }

    @Test
    @DisplayName("3. Duplicate Flight Number Validation Test")
    public void testDuplicateFlightNumber() {
        CreateFlightRequest request = CreateFlightRequest.builder()
                .flightNumber("AI303")
                .legs(Collections.singletonList(
                        CreateFlightLegRequest.builder()
                                .source("Chennai")
                                .destination("Delhi")
                                .scheduledDepartureTime(LocalDateTime.now().plusHours(1))
                                .scheduledArrivalTime(LocalDateTime.now().plusHours(3))
                                .build()
                ))
                .build();

        flightService.createFlight(request);

        assertThatThrownBy(() -> flightService.createFlight(request))
                .isInstanceOf(InvalidFlightException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("4. Invalid Schedule & Same Source/Destination Validation Test")
    public void testInvalidScheduleAndSourceDest() {
        // Source == Destination
        CreateFlightRequest reqSameSrcDest = CreateFlightRequest.builder()
                .flightNumber("AI404")
                .legs(Collections.singletonList(
                        CreateFlightLegRequest.builder()
                                .source("Chennai")
                                .destination("Chennai")
                                .scheduledDepartureTime(LocalDateTime.now().plusHours(1))
                                .scheduledArrivalTime(LocalDateTime.now().plusHours(3))
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> flightService.createFlight(reqSameSrcDest))
                .isInstanceOf(InvalidFlightException.class)
                .hasMessageContaining("Source and destination cannot be identical");

        // Departure time after arrival time
        CreateFlightRequest reqInvalidSchedule = CreateFlightRequest.builder()
                .flightNumber("AI405")
                .legs(Collections.singletonList(
                        CreateFlightLegRequest.builder()
                                .source("Chennai")
                                .destination("Delhi")
                                .scheduledDepartureTime(LocalDateTime.now().plusHours(5))
                                .scheduledArrivalTime(LocalDateTime.now().plusHours(2))
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> flightService.createFlight(reqInvalidSchedule))
                .isInstanceOf(InvalidFlightException.class)
                .hasMessageContaining("Scheduled departure time must be strictly before scheduled arrival time");
    }

    @Test
    @DisplayName("5. Flight Not Found Test")
    public void testFlightNotFound() {
        assertThatThrownBy(() -> flightService.getFlightByFlightNumber("UNKNOWN999"))
                .isInstanceOf(FlightNotFoundException.class)
                .hasMessageContaining("was not found");
    }
}
