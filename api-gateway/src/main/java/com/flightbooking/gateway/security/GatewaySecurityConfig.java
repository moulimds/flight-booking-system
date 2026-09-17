package com.flightbooking.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    private final JwtGatewayFilter jwtGatewayFilter;

    public GatewaySecurityConfig(JwtGatewayFilter jwtGatewayFilter) {
        this.jwtGatewayFilter = jwtGatewayFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                // 1. Public Auth & Search Endpoints
                .pathMatchers("/api/auth/register", "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/verify-email").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/flights/**", "/api/search/**").permitAll()
                
                // 2. Flight & Aircraft Management (ADMIN / AIRCRAFT_ADMIN)
                .pathMatchers("/api/flights/**", "/api/aircraft/**").hasAnyRole("ADMIN", "AIRCRAFT_ADMIN")
                
                // 3. Customer Booking & Payment (CUSTOMER)
                .pathMatchers("/api/booking/**", "/api/payment/**").hasRole("CUSTOMER")
                
                // 4. Check-in Service (Staff roles only)
                .pathMatchers("/api/checkin/**").hasAnyRole("ADMIN", "CUSTOMER_SUPPORT", "OPERATOR")
                
                // 5. Admin User Management (ADMIN)
                .pathMatchers("/api/users/**").hasRole("ADMIN")
                
                // Any other endpoint requires valid authentication
                .anyExchange().authenticated()
            )
            .addFilterAt(jwtGatewayFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }
}
