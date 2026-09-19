package com.flight.fare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Fare Service - Owned by SINDHU
 */
@SpringBootApplication
@EnableDiscoveryClient
public class FareServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FareServiceApplication.class, args);
    }
}
