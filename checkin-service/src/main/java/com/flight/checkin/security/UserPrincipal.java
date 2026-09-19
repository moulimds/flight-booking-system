package com.flight.checkin.security;

import java.io.Serializable;

public class UserPrincipal implements Serializable {

    private Long userId;
    private String email;
    private String role;
    private String fullName;

    public UserPrincipal() {
    }

    public UserPrincipal(Long userId, String email, String role, String fullName) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isCustomer() {
        return "CUSTOMER".equalsIgnoreCase(role) || "ROLE_CUSTOMER".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
    }

    public boolean isSupport() {
        return "SUPPORT".equalsIgnoreCase(role) || "ROLE_SUPPORT".equalsIgnoreCase(role);
    }

    public boolean isAirportAdmin() {
        return "AIRPORT_ADMIN".equalsIgnoreCase(role) || "ROLE_AIRPORT_ADMIN".equalsIgnoreCase(role);
    }

    public boolean isAircraftCompanyAdmin() {
        return "AIRCRAFT_COMPANY_ADMIN".equalsIgnoreCase(role) || "ROLE_AIRCRAFT_COMPANY_ADMIN".equalsIgnoreCase(role);
    }
}
