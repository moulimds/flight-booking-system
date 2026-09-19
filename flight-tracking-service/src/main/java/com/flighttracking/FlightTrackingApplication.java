package com.flighttracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlightTrackingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightTrackingApplication.class, args);
    }
}
