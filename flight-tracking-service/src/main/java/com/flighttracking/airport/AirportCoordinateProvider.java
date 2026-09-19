package com.flighttracking.airport;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AirportCoordinateProvider {

    private final Map<String, AirportCoordinate> airportMap = new HashMap<>();

    public AirportCoordinateProvider() {
        // Standard coordinates for common airports
        registerAirport("CHENNAI", 13.0827, 80.2707);
        registerAirport("MUMBAI", 19.0760, 72.8777);
        registerAirport("DELHI", 28.5562, 77.1000);
        registerAirport("KOLKATA", 22.6547, 88.4467);
        registerAirport("BANGALORE", 12.9716, 77.5946);
        registerAirport("BENGALURU", 12.9716, 77.5946);
        registerAirport("HYDERABAD", 17.3850, 78.4867);
        registerAirport("GOA", 15.3800, 73.8318);
    }

    private void registerAirport(String name, double lat, double lon) {
        airportMap.put(name.toUpperCase(), new AirportCoordinate(name, lat, lon));
    }

    public AirportCoordinate getCoordinate(String airportName) {
        if (airportName == null) {
            return new AirportCoordinate("DEFAULT", 13.0827, 80.2707);
        }
        String key = airportName.trim().toUpperCase();
        if (airportMap.containsKey(key)) {
            return airportMap.get(key);
        }
        // Fallback for unknown airports: hash based position around India region
        double fallbackLat = 15.0 + (Math.abs(key.hashCode()) % 1500) / 100.0;
        double fallbackLon = 73.0 + (Math.abs(key.hashCode()) % 1500) / 100.0;
        return new AirportCoordinate(airportName, fallbackLat, fallbackLon);
    }
}
