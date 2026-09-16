package com.flightbooking.auth.controller;

import com.flightbooking.auth.dto.*;
import com.flightbooking.auth.model.User;
import com.flightbooking.auth.security.JwtService;
import com.flightbooking.auth.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService service;
	private final JwtService jwtService;

	public AuthController(AuthService service, JwtService jwtService) {
		this.service = service;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
		User u = service.register(request);
		return ResponseEntity.status(201).body(Map.of(
			"id", u.getId(),
			"email", u.getEmail(),
			"role", u.getRole(),
			"message", "Registration successful. A 6-digit verification code has been sent to your email."
		));
	}

	@PostMapping("/login")
	public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
		String token = service.login(request);
		return Map.of("accessToken", token, "tokenType", "Bearer");
	}

	@PostMapping("/logout")
	public Map<String, String> logout() {
		return Map.of("message", "Logout acknowledged.");
	}

	@PostMapping("/refresh-token")
	public Map<String, String> refreshToken(Authentication authentication) {
		User u = service.currentUser(Long.valueOf(authentication.getName()));
		return Map.of("accessToken", jwtService.generate(u), "tokenType", "Bearer");
	}

	@PostMapping("/forgot-password")
	public Map<String, String> forgotPassword(@RequestParam String email) {
		String otp = service.forgotPassword(email);
		return Map.of("message", "Password reset OTP has been sent to your email", "otp", otp);
	}

	@PostMapping("/reset-password")
	public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		service.resetPassword(request);
		return Map.of("message", "Password reset successfully");
	}

	@PostMapping("/verify-email")
	public Map<String, String> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
		service.verifyEmailByOtp(request);
		return Map.of("message", "Email verified successfully");
	}

	@PostMapping("/validate-token")
	public Map<String, Object> validateToken(Authentication authentication) {
		User u = service.currentUser(Long.valueOf(authentication.getName()));
		return Map.of("valid", true, "userId", u.getId(), "email", u.getEmail(), "role", u.getRole());
	}

	@GetMapping("/me")
	public User me(Authentication authentication) {
		return service.currentUser(Long.valueOf(authentication.getName()));
	}

	@PostMapping("/change-password")
	public Map<String, String> changePassword(Authentication authentication,
			@Valid @RequestBody ChangePasswordRequest request) {
		service.changePassword(Long.valueOf(authentication.getName()), request);
		return Map.of("message", "Password changed successfully");
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public java.util.List<User> users() {
		return service.allUsers();
	}

	@PostMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> createUserByAdmin(@Valid @RequestBody AdminRegisterRequest request) {
		User u = service.adminRegister(request);
		return ResponseEntity.status(201).body(Map.of("id", u.getId(), "email", u.getEmail(), "role", u.getRole()));
	}

	@GetMapping("/users/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','CUSTOMER_SUPPORT')")
	public User user(@PathVariable Long id) {
		return service.currentUser(id);
	}

	@PutMapping("/users/{id}/role")
	@PreAuthorize("hasRole('ADMIN')")
	public User role(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
		return service.changeRole(id, request.role());
	}

	@DeleteMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Map<String, String> delete(@PathVariable Long id) {
		service.deleteUser(id);
		return Map.of("message", "User deleted successfully", "userId", String.valueOf(id));
	}
}
