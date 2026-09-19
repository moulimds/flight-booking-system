package com.flight.checkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Check-in Service - Owned by ABINESH
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CheckinServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckinServiceApplication.class, args);
    }
}
