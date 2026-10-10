# AirLink — Flight Booking System Frontend

A modern, responsive **Angular frontend application** for the Flight Booking System, designed to provide a seamless experience for flight search, seat selection, booking management, payment, ticket access, and flight tracking.

The frontend follows a component-based architecture using Angular, TypeScript, Angular Router, Reactive Forms, RxJS, and reusable UI components. It integrates with the backend microservices through HTTP APIs to support the complete flight-booking workflow.

## Project Overview

The AirLink frontend provides a unified interface for customers and administrators to interact with the Flight Booking System.

The application includes interfaces for:

- User authentication and registration
- Flight search and filtering
- Passenger information management
- Interactive seat selection
- Booking and payment workflows
- Customer profile management
- Digital boarding passes
- Flight tracking
- Administrative dashboards
- Airport and fleet management
- Customer support
- Notifications and status updates

The frontend is organized into feature-specific components, shared UI elements, services, directives, pipes, route guards, and HTTP interceptors to support maintainability and code reuse.

## Architecture

The frontend communicates with the backend through the API Gateway, which routes requests to the appropriate microservices.

```text
                  +----------------------+
                  |      User / Admin    |
                  +----------+-----------+
                             |
                             v
                  +----------------------+
                  |   Angular Frontend   |
                  |    AirLink UI        |
                  |     :4200            |
                  +----------+-----------+
                             |
                             v
                  +----------------------+
                  |     API Gateway      |
                  |       :8080          |
                  +----------+-----------+
                             |
           +-----------------+------------------+
           |                 |                  |
           v                 v                  v
     Auth Service      Flight Service     Booking Service
           |                 |                  |
           +-----------------+------------------+
                             |
                 +-----------+-----------+
                 |                       |
                 v                       v
          +-------------+         +-------------+
          |   Eureka    |         |   Config    |
          |   :8761     |         |   :8888     |
          +-------------+         +-------------+
```

The Angular application handles presentation, user interaction, client-side validation, navigation, and API communication. Business rules, persistent data, authentication enforcement, and transaction processing remain responsibilities of the backend services.

## Frontend Features

| No. | Feature | Responsibility |
|---:|---|---|
| 1 | Landing Page | Introduces the application and provides access to flight search |
| 2 | Authentication | Login, registration, and authentication-related UI |
| 3 | Flight Search | Search flights and display available results |
| 4 | Flight Filtering | Filter results by airline, price, cabin class, and departure time |
| 5 | Passenger Details | Collect and validate passenger information |
| 6 | Seat Selection | Display cabin layouts and seat availability states |
| 7 | Payment | Provide the checkout interface and display payment results |
| 8 | Profile Management | Display and manage customer profile information |
| 9 | Digital Boarding Pass | Present ticket and boarding-pass information |
| 10 | Flight Tracking | Display flight status and tracking information |
| 11 | Admin Dashboard | Present flight, booking, and revenue summaries |
| 12 | Airport Console | Provide airport-related administrative interfaces |
| 13 | Fleet Console | Provide fleet-management interfaces |
| 14 | Customer Support | Display support-related functionality |
| 15 | Shared Components | Reusable navigation, footer, sidebar, and toast notifications |

Feature availability and behavior depend on the implementation of the corresponding components and their backend integrations.

## UI Design and Reference Implementations

The frontend combines a modern travel-oriented interface with practical workflows for customers and administrators.

### 1. Flight Search Dashboard

**Component:** `src/app/features/flights/search-dashboard/`

The dashboard helps users discover and compare available flights.

Key UI elements include:

- Flight search form
- Origin and destination information
- Flight result cards
- Departure and arrival times
- Fare and duration information
- Cabin-class filters
- Airline selection
- Price-range filtering
- Departure-time filters
- Route information and aircraft details

### 2. Landing Page

**Component:** `src/app/features/landing/`

The landing page provides the application's primary entry point.

Design elements include:

- Travel-inspired hero section
- Aircraft visuals and promotional content
- Flight search interface
- Origin, destination, and travel-date fields
- Featured routes and popular destinations

### 3. Seat Selection

**Component:** `src/app/features/booking/seat-selection/`

The seat-selection interface presents available seating options visually.

Features include:

- Aircraft cabin layout
- Business and Economy cabin sections
- Available, selected, and occupied seat states
- Seat selection and details
- Seat-related pricing information

Seat availability and reservation rules are determined by the backend integration.

### 4. Administrative Dashboard

**Component:** `src/app/features/admin/admin-dashboard/`

The dashboard provides a centralized view of administrative information.

It is designed to present:

- Flight and booking metrics
- Revenue summaries
- Weekly ticket-sales information
- Airline statistics
- Booking-management information

### 5. Boarding Pass and Flight Tracking

**Components:**

- `src/app/features/ticket/boarding-pass/`
- `src/app/features/tracking/flight-radar/`

These interfaces provide access to ticket information and flight status.

The boarding-pass interface can display flight details, seat information, passenger information, and boarding details when available. The tracking interface presents flight telemetry and route progression when supported by the connected data source.

## Angular Architecture

The frontend uses a feature-oriented, component-based structure.

### Standalone Components

Angular standalone components allow individual components to declare their dependencies through their own `imports` configuration.

This approach reduces the need for NgModule-based component registration and supports a modular application structure.

### Application Configuration

The application configuration is organized around:

- `app.config.ts` — Application providers and configuration
- `app.routes.ts` — Route definitions and navigation
- `app.component.ts` — Root application component

The application may use Angular providers such as `provideRouter()` and `provideHttpClient()` according to its configuration.

### Core Layer

The `core/` directory organizes application-wide services and infrastructure, including:

- Authentication and application services
- Route guards
- HTTP interceptors

### Features Layer

The `features/` directory contains the components associated with individual business workflows, such as authentication, booking, flight search, payment, profiles, ticketing, and administration.

### Shared Layer

The `shared/` directory contains reusable components, directives, and pipes used across multiple features.

## Services and Dependency Injection

Angular services separate API communication and reusable application logic from presentation components.

| Service | Responsibility |
|---|---|
| `AuthService` | Authentication and current-user state |
| `FlightService` | Flight search and fare-related requests |
| `SeatService` | Seat information and reservation-related operations |
| `BookingService` | Booking creation, retrieval, and cancellation |
| `PaymentService` | Payment workflow and transaction references |
| `TrackingService` | Flight-tracking data and periodic updates |
| `ProfileService` | Customer profile and passport information |
| `ToastService` | Application notifications and user feedback |

Services use Angular dependency injection to make shared functionality available to components and other services.

The exact operations and API endpoints are defined by the implementation of each service.

## Templates, Directives, and Pipes

### Custom Directives

| Directive | Purpose |
|---|---|
| `appSeatStatus` | Applies visual styling to seat-status states |
| `appStatusBadge` | Provides consistent status-badge styling |
| `appClickOutside` | Supports dismissing menus and overlays when clicking outside them |

### Custom Pipes

| Pipe | Purpose |
|---|---|
| `flightDuration` | Formats flight-duration values |
| `airportName` | Converts airport codes into display labels using available mappings |
| `currencyFormatter` | Formats currency values for display |
| `flightFilter` | Supports filtering flight data according to configured criteria |

The shared directives and pipes help reduce duplicated template logic and maintain consistent presentation.

## Forms and Validation

The application uses Angular forms to collect information and validate user input.

### Reactive Forms

Reactive forms are suitable for structured forms that require explicit control over validation and form state.

They are used or intended for workflows such as:

- Passenger details
- Payment checkout
- Customer profile management

Angular form utilities include `FormBuilder`, `FormGroup`, and `Validators`.

### Template-Driven Forms

Template-driven forms use directives such as `ngModel` for form binding.

They can support simpler interactions, including flight-search inputs and filter controls, where configured in the relevant components.

### Validation

Form validation may include:

- Required fields
- Email format validation
- Phone-number constraints
- Passport information
- Payment-field validation
- Required passenger information

Client-side validation improves the user experience, while the backend remains responsible for validating requests and enforcing business rules.

## Routing and Navigation

The application uses Angular Router to navigate between feature pages.

### Lazy Loading

The route configuration can use `loadComponent` to load standalone components when their routes are accessed.

### Dynamic Routes

Examples of route patterns include:

- `/booking/seats/:id`
- `/tickets/boarding-pass/:reference`
- `/profile`
- `/admin/dashboard`

The exact paths depend on the application's route configuration.

### Route Guards

- `authGuard` — Controls navigation to protected user pages.
- `adminGuard` — Controls navigation to administrative pages based on configured permissions.

Route guards control frontend navigation. Backend APIs must independently enforce authentication and authorization.

## HTTP Communication and RxJS

The frontend communicates with backend microservices through Angular's HTTP client.

### HTTP Interceptors

| Interceptor | Responsibility |
|---|---|
| `authInterceptor` | Adds the configured authentication token to eligible requests |
| `errorInterceptor` | Centralizes HTTP error handling and can trigger user notifications |

### RxJS

RxJS supports asynchronous operations and reactive UI updates.

The project references operators and utilities such as:

- `map`
- `tap`
- `switchMap`
- `catchError`
- `startWith`
- `filter`
- `BehaviorSubject`
- `interval`

These can be used to transform API responses, handle errors, maintain reactive state, and periodically retrieve tracking information.

## Technologies Used

### Frontend

| Technology | Purpose |
|---|---|
| Angular 17/18 target | Frontend framework |
| TypeScript | Application logic and type safety |
| HTML5 | Page structure |
| CSS3 | Styling and responsive layouts |
| Angular Router | Navigation and lazy loading |
| Angular Forms | Form handling and validation |
| Angular HttpClient | Backend API communication |
| RxJS | Reactive programming and asynchronous operations |

### Development Tools

| Tool | Purpose |
|---|---|
| Node.js | JavaScript runtime for frontend tooling |
| npm | Dependency management |
| Angular CLI | Development server, builds, and project tooling |
| Git | Version control |
| GitHub | Source code hosting and collaboration |
| VS Code | Frontend development environment |

**Version note:** Confirm the installed Angular version in `package.json` before installing dependencies or using version-specific features.

## Project Structure

```text
flight-booking-system/
|
+-- src/
|   +-- app/
|   |   +-- core/
|   |   |   +-- guards/
|   |   |   +-- interceptors/
|   |   |   +-- services/
|   |   |
|   |   +-- features/
|   |   |   +-- admin/
|   |   |   |   +-- admin-dashboard/
|   |   |   |   +-- airport-console/
|   |   |   |   +-- fleet-console/
|   |   |   |   +-- support-console/
|   |   |   |
|   |   |   +-- auth/
|   |   |   |   +-- login/
|   |   |   |   +-- register/
|   |   |   |
|   |   |   +-- booking/
|   |   |   |   +-- passenger-details/
|   |   |   |   +-- seat-selection/
|   |   |   |
|   |   |   +-- flights/
|   |   |   |   +-- search-dashboard/
|   |   |   |
|   |   |   +-- landing/
|   |   |   +-- payment/
|   |   |   +-- profile/
|   |   |   +-- support/
|   |   |   +-- ticket/
|   |   |   |   +-- boarding-pass/
|   |   |   +-- tracking/
|   |   |       +-- flight-radar/
|   |   |
|   |   +-- shared/
|   |   |   +-- components/
|   |   |   |   +-- footer/
|   |   |   |   +-- navbar/
|   |   |   |   +-- sidebar/
|   |   |   |   +-- toast/
|   |   |   +-- directives/
|   |   |   +-- pipes/
|   |   |
|   |   +-- app.component.ts
|   |   +-- app.config.ts
|   |   +-- app.routes.ts
|   |
|   +-- assets/
|   +-- environments/
|
+-- angular.json
+-- package.json
+-- tsconfig.json
+-- README.md
```

This is a representative structure based on the documented features; optional files and nested directories may differ in the repository.

## Running the Frontend

### Prerequisites

Ensure the following tools are installed:

- Node.js compatible with the project's Angular version
- npm
- Git
- Access to the required backend services for API integration

### 1. Clone the Repository

```bash
git clone https://github.com/moulimds/flight-booking-system.git
cd flight-booking-system
```

### 2. Navigate to the Frontend

Run the following command from the directory containing the Angular application's `package.json`:

```bash
cd frontend
```

If the Angular application is located at the repository root, remain in the root directory instead.

### 3. Install Dependencies

```bash
npm install
```

### 4. Start the Development Server

If the `package.json` contains an `npm start` script:

```bash
npm start
```

Alternatively, use Angular CLI:

```bash
npx ng serve
```

### 5. Open the Application

Visit:

**http://localhost:4200**

The frontend requires the appropriate environment configuration and running backend services for integrated functionality.

## Backend Integration

The frontend is designed to communicate with the backend microservices through the API Gateway.

Typical interactions include:

- Authentication and user management
- Flight search and management
- Fare retrieval
- Seat availability
- Booking creation and retrieval
- Payment operations
- Invoice and ticket information
- Notifications
- Customer profile management
- Check-in
- Flight tracking

API base URLs should be configured through the application's environment or deployment configuration.

The frontend does not replace backend validation, authorization, database operations, or transaction processing.

## API Testing

Frontend integration can be tested alongside the backend APIs using Postman.

Important workflows to verify include:

- Login and registration
- Flight search
- Flight selection
- Seat selection
- Passenger details
- Booking creation
- Payment outcomes
- Ticket and invoice retrieval
- Profile management
- Check-in
- Flight tracking

Testing should cover successful requests, invalid inputs, expired sessions, unavailable services, and server-side errors.

## Key Features

- Angular component-based architecture
- Feature-oriented project organization
- Reusable shared UI components
- Responsive travel-oriented interface
- Flight search and filtering
- Passenger forms and validation
- Interactive seat-selection interface
- Payment and booking workflows
- Customer profile management
- Digital boarding-pass presentation
- Flight-tracking interface
- Administrative dashboards
- Custom directives and pipes
- Angular Router and lazy loading
- Route guards and HTTP interceptors
- RxJS-based asynchronous operations
- Integration with backend microservices

## Conclusion

The AirLink frontend brings the user-facing side of the Flight Booking System together through a structured Angular application.

By combining reusable components, feature-oriented organization, form validation, routing, reactive programming, and backend API integration, the frontend provides a foundation for a maintainable and intuitive flight-booking experience.

**Built with Angular and TypeScript, with a focus on clean UI design, modular development, and seamless travel workflows.**
