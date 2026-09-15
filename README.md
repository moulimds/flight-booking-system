# Flight Booking System - Auth Service + Check-in Service

This package contains the Auth Service and Check-in Service microservices for the Flight Booking System, configured to use **MySQL Database** with `application.properties` configuration and tested via Postman.

---

## 🗑️ Admin User Management & Deletion

- **Delete User:** `DELETE /api/users/{userId}` is strictly restricted to **`ADMIN`** (`@PreAuthorize("hasRole('ADMIN')")`).
- **List Users:** `GET /api/users` is restricted to **`ADMIN`**.
- **Get User:** `GET /api/users/{userId}` is accessible by **`ADMIN`** and **`CUSTOMER_SUPPORT`**.

---

## ✈️ Check-in Service Access Control

The entire **Check-in Service** (`checkin-service` on port `8082`) is strictly restricted to staff roles:
- **`ADMIN`**
- **`CUSTOMER_SUPPORT`**
- **`OPERATOR`**

Requests made by `CUSTOMER` or `AIRCRAFT_ADMIN` to Check-in endpoints will be rejected with `403 Forbidden`.

---

## 🔑 Default Admin Account Credentials

The **Auth Service** automatically initializes a default Administrator user upon startup:

| Property | Default Value |
| :--- | :--- |
| **Email** | `admin@example.com` |
| **Password** | `AdminPass123` |
| **Role** | `ADMIN` |
| **Active** | `true` |

---

## 📬 Postman API Collection & Testing

A complete, standard Postman Collection v2.1.0 JSON file is provided in the root directory:

📄 **[flight_booking_postman_collection.json](file:///c:/Capgemini_Sprint_WorkShop/flight-booking-auth-checkin/flight_booking_postman_collection.json)**
