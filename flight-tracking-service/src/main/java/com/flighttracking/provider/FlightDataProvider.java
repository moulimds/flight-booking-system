package com.flighttracking.provider;

import com.flighttracking.airport.AirportCoordinate;
import com.flighttracking.entity.FlightTracking;

public interface FlightDataProvider {

    AirportCoordinate getAirportCoordinates(String airportName);

    double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2);

    double calculateBearing(double lat1, double lon1, double lat2, double lon2);

    FlightTracking updatePosition(FlightTracking currentTracking, AirportCoordinate source, AirportCoordinate destination);
}
