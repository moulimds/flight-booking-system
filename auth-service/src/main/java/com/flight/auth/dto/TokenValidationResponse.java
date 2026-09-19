package com.flight.auth.dto;

import com.flight.auth.entity.Role;

public class TokenValidationResponse {

    private boolean valid;
    private String email;
    private Role role;
    private Long userId;

    public TokenValidationResponse() {
    }

    public TokenValidationResponse(boolean valid, String email, Role role, Long userId) {
        this.valid = valid;
        this.email = email;
        this.role = role;
        this.userId = userId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
