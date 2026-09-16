package com.flightbooking.auth.dto;

import com.flightbooking.auth.model.Role;
import jakarta.validation.constraints.NotNull;

public record RoleRequest(@NotNull Role role) {}
