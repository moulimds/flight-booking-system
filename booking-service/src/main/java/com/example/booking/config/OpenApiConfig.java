package com.example.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flight Booking System — Booking Service API")
                        .description("REST APIs for managing flight reservations, passenger manifest, status audits, and RabbitMQ asynchronous messaging.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Flight Booking System Platform Engineering")
                                .email("engineering@flightbooking.example.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
