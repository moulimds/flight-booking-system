# ✈️ AirLink / PLANNED / Skytix — Angular Enterprise Frontend

A modern, production-ready, responsive **Angular 17/18** frontend application for the **Flight Booking System Microservices**.

---

## 🎨 Reference UI Design Implementations

| Reference Image | Feature / Page | Implemented Component & Files |
| :--- | :--- | :--- |
| **Reference 1: `PLANNED` Web Dashboard** | Left navigation sidebar (`Home`, `Ticket`, `Schedule`, `History`, `Settings`), Search Flight Widget, Multi-filter side panel (Cabin Class pills, Price Range slider, Airlines checkboxes, Departure slots), Route badge `EG ---✈--- KSA`, Flight cards with timing, price tags, and aircraft badges. | `src/app/features/flights/search-dashboard/search-dashboard.component.ts`<br>`src/app/shared/components/sidebar/sidebar.component.ts` |
| **Reference 2: `AirLink` Luxury Hero Landing** | Fluid hero banner with aircraft visual, luxury typography (*"Your Ticket to Explore the World"*), floating glassmorphic search bar widget (`From`, `To`, `Departure`, `Return`, search button), featured popular routes. | `src/app/features/landing/landing.component.ts` |
| **Reference 3: Aircraft Cabin Seat Selection Map** | Fuselage silhouette, cockpit window graphic, Business Class (2×2) and Economy Class (3×3) seating grid, real-time seat status color coding (Available, Selected, Occupied), seat details drawer, dynamic price calculation. | `src/app/features/booking/seat-selection/seat-selection.component.ts`<br>`src/app/shared/directives/seat-status.directive.ts` |
| **Reference 4: `Skytix` Executive Operations Dashboard** | KPI metric cards (Total Flights, Booked Flights, Cancelled Flights, Total Revenue with trend indicators), Weekly Ticket Sales bar chart, Popular Airlines share progress bars, Bookings data management table. | `src/app/features/admin/admin-dashboard/admin-dashboard.component.ts`<br>`src/app/core/services/admin.service.ts` |
| **Reference 5: Electronic Boarding Pass & Live Radar** | Digital Boarding Pass with barcode graphic, flight number, gate, seat, seat class, and live real-time flight radar tracking flight altitude, ground speed, heading, and route progression. | `src/app/features/ticket/boarding-pass/boarding-pass.component.ts`<br>`src/app/features/tracking/flight-radar/flight-radar.component.ts` |

---

## 🎓 Angular Learning Curriculum Concepts in Codebase

### 1. Introduction to Angular & Essentials
- **Standalone Components:** All components (`LandingComponent`, `SearchDashboardComponent`, `SeatSelectionComponent`, etc.) use modern Angular Standalone Component architecture.
- **Application Config:** Standalone bootstrapping via `app.config.ts` using `provideRouter`, `provideHttpClient`, and `provideAnimations`.

### 2. Templates, Styles & Directives
- **Custom Directives:**
  - `[appSeatStatus]`: `src/app/shared/directives/seat-status.directive.ts` (manages aircraft seat DOM states).
  - `[appStatusBadge]`: `src/app/shared/directives/status-badge.directive.ts` (styles status pills for `CONFIRMED`, `PENDING`, `CANCELLED`).
  - `[appClickOutside]`: `src/app/shared/directives/click-outside.directive.ts` (dismisses dropdowns/modals).
- **Modern Control Flow & Directives:** Uses `@if`, `@for`, `@switch` alongside structural directives `*ngIf` and `*ngFor`.

### 3. Custom & Built-in Pipes
- `flightDuration`: `src/app/shared/pipes/flight-duration.pipe.ts` (transforms ISO timestamps into `"2h 50m"`).
- `airportName`: `src/app/shared/pipes/airport-name.pipe.ts` (maps IATA codes like `EG` → `"Egypt Airline Airport (CAI)"`).
- `currencyFormatter`: `src/app/shared/pipes/currency-formatter.pipe.ts` (formats `$ 600` or `₹ 5,499`).
- `flightFilter`: `src/app/shared/pipes/flight-filter.pipe.ts` (filters flights by airline, price, and cabin).

### 4. Services & Dependency Injection
- `AuthService`: `src/app/core/services/auth.service.ts` (manages JWT tokens and `currentUser$` `BehaviorSubject`).
- `FlightService`: `src/app/core/services/flight.service.ts` (fetches flight search results and fares).
- `SeatService`: `src/app/core/services/seat.service.ts` (manages seat layouts, holding and releasing seats).
- `BookingService`: `src/app/core/services/booking.service.ts` (creates bookings, lists tickets, cancels reservations).
- `PaymentService`: `src/app/core/services/payment.service.ts` (processes payments and generates transaction references).
- `TrackingService`: `src/app/core/services/tracking.service.ts` (live radar telemetry polling with RxJS `interval`).
- `ProfileService`: `src/app/core/services/profile.service.ts` (manages verified customer profile and passport data).
- `ToastService`: `src/app/core/services/toast.service.ts` (reactive notification system).

### 5. Template-Driven & Reactive Forms
- **Reactive Forms with FormBuilder & Validators:**
  - `PassengerDetailsComponent`: `src/app/features/booking/passenger-details/passenger-details.component.ts` (validates full name, email, phone number).
  - `CheckoutComponent`: `src/app/features/payment/checkout/checkout.component.ts` (validates card number, expiry, CVV).
  - `ProfileViewComponent`: `src/app/features/profile/profile-view/profile-view.component.ts` (validates passport number, country, expiration).
- **Template-Driven & Two-Way Binding (`[(ngModel)]`):**
  - `SearchDashboardComponent`: Filters, price range slider, cabin pills, passenger counters.

### 6. Components Deep Dive & Routing
- **Lazy Loaded Routes:** Configured in `src/app/app.routes.ts` using `loadComponent`.
- **Route Parameters:** Dynamic routes `:id` (e.g. `/booking/seats/:id`) and `:reference` (e.g. `/tickets/boarding-pass/:reference`).
- **Layout Switching:** Dynamic master-detail layout toggle in `src/app/app.component.ts`.

### 7. HTTP Requests & RxJS Observables
- **Functional Interceptors:**
  - `authInterceptor`: `src/app/core/interceptors/auth.interceptor.ts` (automatically attaches `Authorization: Bearer <token>`).
  - `errorInterceptor`: `src/app/core/interceptors/error.interceptor.ts` (catches errors and triggers toast alerts).
- **RxJS Operators:** `map`, `tap`, `switchMap`, `catchError`, `startWith`, `filter`, `BehaviorSubject`.

### 8. Authentication & Route Protection
- **Functional Route Guards:**
  - `authGuard`: `src/app/core/guards/auth.guard.ts` (protects private user routes like `/profile` and `/tickets`).
  - `adminGuard`: `src/app/core/guards/auth.guard.ts` (protects `/admin/dashboard` requiring `ROLE_ADMIN`).
- **Quick Fill Demo Accounts:** Instant one-click Customer and Admin credentials in `src/app/features/auth/login/login.component.ts`.

---

## 🚀 How to Run the Frontend

1. Open your terminal in the `frontend` directory:
   ```bash
   cd "d:\Capgemini per-onboarding\Flight-Booking-System\frontend"
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the Angular Development Server:
   ```bash
   npm start
   ```

4. Open your browser at:
   ```
   http://localhost:4200
   ```
