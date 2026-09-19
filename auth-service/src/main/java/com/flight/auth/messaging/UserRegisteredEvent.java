package com.flight.auth.messaging;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UserRegisteredEvent implements Serializable {

    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime timestamp;

    public UserRegisteredEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public UserRegisteredEvent(Long userId, String fullName, String email, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.timestamp = LocalDateTime.now();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
