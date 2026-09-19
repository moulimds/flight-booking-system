<<<<<<<< HEAD:auth-service/src/main/java/com/flight/auth/AuthServiceApplication.java
package com.flight.auth;
========
package com.flight.booking;
>>>>>>>> origin/feature/booking:booking-service/src/main/java/com/flight/booking/BookingServiceApplication.java

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
<<<<<<<< HEAD:auth-service/src/main/java/com/flight/auth/AuthServiceApplication.java
 * Authentication Service - Owned by ABINESH
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {
========
 * Booking Service - Owned by SINDHU
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class BookingServiceApplication {
>>>>>>>> origin/feature/booking:booking-service/src/main/java/com/flight/booking/BookingServiceApplication.java

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
