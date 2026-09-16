package com.airline.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flight Booking - Notification Service API")
                        .description("Microservice responsible for multi-channel message dispatch (Email/SMS), template rendering, delivery audit logs, and asynchronous RabbitMQ event consumption.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Airline Engineering Platform Team")
                                .email("dev-support@airline.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway URL"),
                        new Server().url("http://localhost:8085").description("Direct Notification Service URL")
                ));
    }
}
