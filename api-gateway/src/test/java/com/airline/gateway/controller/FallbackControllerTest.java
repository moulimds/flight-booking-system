package com.airline.gateway.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(FallbackController.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("GET /fallback/seat - Returns 503 SERVICE_UNAVAILABLE fallback payload")
    void seatServiceFallback_ReturnsServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/seat")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.service").isEqualTo("SEAT-SERVICE")
                .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.error").isEqualTo("Service Unavailable");
    }

    @Test
    @DisplayName("GET /fallback/notification - Returns 503 SERVICE_UNAVAILABLE fallback payload")
    void notificationServiceFallback_ReturnsServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/notification")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.service").isEqualTo("NOTIFICATION-SERVICE")
                .jsonPath("$.status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
                .jsonPath("$.error").isEqualTo("Service Unavailable");
    }
}
