package com.flighttracking.simulator;

import com.flighttracking.airport.AirportCoordinate;
import com.flighttracking.airport.AirportCoordinateProvider;
import com.flighttracking.entity.FlightTracking;
import com.flighttracking.provider.FlightDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MockFlightDataProvider implements FlightDataProvider {

    private final AirportCoordinateProvider airportCoordinateProvider;

    private static final double EARTH_RADIUS_KM = 6371.0;
    // Step ratio towards destination per 5-second tick (25% per tick for smooth & fast test simulation)
    private static final double STEP_RATIO = 0.25;

    @Override
    public AirportCoordinate getAirportCoordinates(String airportName) {
        return airportCoordinateProvider.getCoordinate(airportName);
    }

    @Override
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dLon = Math.toRadians(lon2 - lon1);

        double y = Math.sin(dLon) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2) -
                Math.sin(phi1) * Math.cos(phi2) * Math.cos(dLon);

        double brng = Math.toDegrees(Math.atan2(y, x));
        return (brng + 360) % 360;
    }

    @Override
    public FlightTracking updatePosition(FlightTracking currentTracking, AirportCoordinate source, AirportCoordinate destination) {
        double curLat = currentTracking.getLatitude();
        double curLon = currentTracking.getLongitude();

        double destLat = destination.getLatitude();
        double destLon = destination.getLongitude();

        double remainingDistance = calculateDistanceKm(curLat, curLon, destLat, destLon);
        double heading = calculateBearing(curLat, curLon, destLat, destLon);

        if (remainingDistance < 5.0) {
            // Reached destination threshold
            currentTracking.setLatitude(destLat);
            currentTracking.setLongitude(destLon);
            currentTracking.setAltitude(0.0);
            currentTracking.setSpeed(0.0);
            currentTracking.setHeading(heading);
            currentTracking.setDistanceRemaining(0.0);
            currentTracking.setUpdatedAt(LocalDateTime.now());
            return currentTracking;
        }

        // Interpolate position towards destination
        double newLat = curLat + (destLat - curLat) * STEP_RATIO;
        double newLon = curLon + (destLon - curLon) * STEP_RATIO;

        double updatedDistance = calculateDistanceKm(newLat, newLon, destLat, destLon);

        if (updatedDistance < 5.0) {
            newLat = destLat;
            newLon = destLon;
            updatedDistance = 0.0;
        }

        double cruisingAltitude = 32000.0; // 32,000 feet cruising altitude
        double cruisingSpeed = 450.0;     // 450 knots cruising speed

        currentTracking.setLatitude(Math.round(newLat * 10000.0) / 10000.0);
        currentTracking.setLongitude(Math.round(newLon * 10000.0) / 10000.0);
        currentTracking.setAltitude(updatedDistance == 0.0 ? 0.0 : cruisingAltitude);
        currentTracking.setSpeed(updatedDistance == 0.0 ? 0.0 : cruisingSpeed);
        currentTracking.setHeading(Math.round(heading * 10.0) / 10.0);
        currentTracking.setDistanceRemaining(Math.round(updatedDistance * 10.0) / 10.0);
        currentTracking.setUpdatedAt(LocalDateTime.now());

        return currentTracking;
    }
}
