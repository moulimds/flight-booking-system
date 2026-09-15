package com.flightbooking.auth.dto;

import com.flightbooking.auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminRegisterRequest(
    @NotBlank @Size(min = 2, max = 100) String fullName,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, max = 100) String password,
    @NotNull Role role
) {}
