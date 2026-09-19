# Flight Booking System

A microservices-based Flight Booking System designed to manage the complete flight-booking workflow through independently deployable services.

The system follows a distributed microservices architecture using Spring Boot, Spring Cloud, Eureka Service Discovery, Spring Cloud Gateway, JWT-based authentication, RabbitMQ, MySQL, and Maven.

---

## Project Overview

The Flight Booking System provides a modular architecture where different business functionalities are handled by dedicated microservices.

The system includes services for:

* User authentication and management
* Flight management
* Flight search
* Flight booking
* Seat management
* Fare management
* Payment processing
* Invoice generation
* Notifications
* User profile management
* Check-in
* Flight tracking
* Centralized configuration
* Service discovery
* API routing

---

## Architecture

```text
                         +---------------------+
                         |     Client / UI     |
                         +----------+----------+
                                    |
                                    v
                         +---------------------+
                         |     API Gateway     |
                         |       :8080         |
                         +----------+----------+
                                    |
              +---------------------+---------------------+
              |                     |                     |
              v                     v                     v
       Auth Service          Flight Service        Booking Service
              |                     |                     |
              +---------------------+---------------------+
                                    |
                         +----------+----------+
                         |                     |
                         v                     v
                +-----------------+    +-----------------+
                | Eureka Server   |    | Config Server   |
                |     :8761       |    |     :8888       |
                +-----------------+    +-----------------+
```

---

## Microservices

| No. | Service                 | Responsibility                          |
| --: | ----------------------- | --------------------------------------- |
|   1 | API Gateway             | Central entry point and request routing |
|   2 | Auth Service            | Authentication and JWT-based security   |
|   3 | Booking Service         | Flight booking operations               |
|   4 | Check-in Service        | Passenger check-in                      |
|   5 | Config Server           | Centralized configuration management    |
|   6 | Eureka Server           | Service discovery and registration      |
|   7 | Fare Service            | Flight fare management                  |
|   8 | Flight Service          | Flight management                       |
|   9 | Flight Tracking Service | Flight tracking                         |
|  10 | Invoice Service         | Invoice generation and management       |
|  11 | Notification Service    | Notifications and email functionality   |
|  12 | Payment Service         | Payment and refund operations           |
|  13 | Profile Service         | User profile management                 |
|  14 | Search Service          | Flight search                           |
|  15 | Seat Service            | Seat management                         |

---

## Project Team

| Team Member | Services / Responsibilities |
|---|---|
| Mouli | Configuration, Invoice, Payment, CI/CD |
| Sindhu | Booking, Fare, Development, Testing (JUnit 5, Mockito) |
| Ashad | Seat, Notification, API Gateway, Eureka |
| Gokul | Flight, Search, Profile |
| Abinesh | Auth, Check-in, Flight Tracking |

---

## Technologies Used

### Backend

* Java 21
* Spring Boot
* Spring MVC
* Spring Data JPA
* Spring Security
* Spring Cloud
* Spring Cloud Gateway
* Spring Cloud Netflix Eureka
* Spring Cloud Config
* JWT
* Maven

### Database

* MySQL

### Messaging

* RabbitMQ

### API Testing

* Postman

### Development and Version Control

* Git
* GitHub
* Eclipse IDE
* Apache Maven

---

## Security

The system uses JWT-based authentication for securing protected APIs.

```text
Client
   |
   v
Auth Service
   |
   v
Authentication
   |
   v
JWT Token
   |
   v
Protected Microservices
```

---

## Service Discovery

The project uses Eureka Server for service registration and discovery.

**Eureka Server:** `http://localhost:8761`

---

## Centralized Configuration

The project includes a dedicated Config Server for centralized configuration management.

**Config Server:** `http://localhost:8888`

---

## API Gateway

The API Gateway acts as the single entry point for client requests and routes requests to the appropriate microservices.

```text
Client
   |
   v
API Gateway
   |
   +-- Auth Service
   +-- Flight Service
   +-- Search Service
   +-- Booking Service
   +-- Payment Service
   +-- Seat Service
   +-- Other Services
```

---

## RabbitMQ

RabbitMQ is used for asynchronous communication between relevant microservices, helping decouple services and support message-based communication.

---

## Database

The application uses MySQL for persistent data storage.

---

## Project Structure

```text
flight-booking-system/
|
+-- api-gateway/
+-- auth-service/
+-- booking-service/
+-- checkin-service/
+-- config-server/
+-- eureka-server/
+-- fare-service/
+-- flight-service/
+-- flight-tracking-service/
+-- invoice-service/
+-- notification-service/
+-- payment-service/
+-- profile-service/
+-- search-service/
+-- seat-service/
+-- pom.xml
+-- README.md
```

---

## Running the Project

### Prerequisites

* Java 21
* Maven
* MySQL
* RabbitMQ
* Git

### Clone the Repository

```bash
git clone https://github.com/moulimds/flight-booking-system.git
```

### Navigate to the Project

```bash
cd flight-booking-system
```

### Build the Project

```bash
mvn clean install -DskipTests
```

### Start Infrastructure Services

Start the following services first:

1. Eureka Server
2. Config Server

Then start the remaining microservices according to their configured ports and dependencies.

---

## High-Level Booking Flow

```text
User
 |
 v
API Gateway
 |
 v
Authentication
 |
 v
Flight Search
 |
 v
Flight Selection
 |
 v
Seat Selection
 |
 v
Booking
 |
 v
Payment
 |
 v
Invoice
 |
 v
Notification
```

---

## API Testing

The APIs can be tested using Postman.

Major operations include:

* User authentication
* User registration
* Flight search
* Flight management
* Seat management
* Booking
* Payment
* Refund
* Check-in
* Invoice generation
* Notifications
* Flight tracking

---

## Key Features

* Microservices-based architecture
* Service discovery using Eureka
* Centralized configuration using Config Server
* API routing through Spring Cloud Gateway
* JWT-based authentication
* Flight search and management
* Booking management
* Seat management
* Fare management
* Payment and refund functionality
* Invoice generation
* Email and notification functionality
* Passenger check-in
* Flight tracking
* RabbitMQ-based messaging
* MySQL persistence
* RESTful APIs
* Postman API testing

---

## Project Status

The project consists of 15 microservices integrated into the `main` branch.

All services are maintained within a single GitHub repository following a modular microservices architecture.

---

## License

This project is developed for academic/project submission purposes.
