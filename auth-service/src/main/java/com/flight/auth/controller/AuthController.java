package com.flight.auth.controller;

import com.flight.auth.dto.*;
import com.flight.auth.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;



    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Public registration requested for email: {}", request.getEmail());
        AuthResponse response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login requested for email: {}", request.getEmail());
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("Forgot password requested for email: {}", request.getEmail());
        MessageResponse response = userService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        logger.info("Reset password requested for email: {}", request.getEmail());
        MessageResponse response = userService.resetPassword(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        logger.info("Get current user profile requested for: {}", email);
        UserResponse response = userService.getCurrentUserProfile(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "anonymous";
        logger.info("User logged out: {}", email);
        return ResponseEntity.ok(new MessageResponse(200, "Successfully logged out"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String token) {
        logger.info("Refresh token requested");
        AuthResponse response = userService.refreshToken(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String token) {
        logger.info("Validate token requested");
        TokenValidationResponse response = userService.validateToken(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(Authentication authentication,
                                                         @Valid @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();
        logger.info("Change password requested for: {}", email);
        MessageResponse response = userService.changePassword(email, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        logger.info("Verify email requested for: {}", request.getEmail());
        MessageResponse response = userService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/forgot-password/otp", "/reset-password/otp", "/request-password-change-email"})
    public ResponseEntity<java.util.Map<String, Object>> sendPasswordResetOtp(@RequestBody(required = false) java.util.Map<String, String> request) {
        String email = (request != null && request.get("email") != null) ? request.get("email") : "sindhusakthi41@gmail.com";
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        logger.info("Generated Password Reset OTP for {}: {}", email, otp);

        try {
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            java.util.Map<String, Object> notifPayload = new java.util.HashMap<>();
            notifPayload.put("recipient", email);
            notifPayload.put("customerName", "Sindhu Sakthivel");
            notifPayload.put("username", email);
            notifPayload.put("otp", otp);
            notifPayload.put("otpExpiry", "10 minutes");
            notifPayload.put("requestDateTime", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy, hh:mm a")));
            notifPayload.put("securityWarning", "Do not share this OTP with anyone, including SkyWings staff.");
            notifPayload.put("subject", "Password Reset OTP");

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(notifPayload, headers);
            rt.postForEntity("http://localhost:8089/api/notifications/password-reset-otp", entity, String.class);
            logger.info("Dispatched password reset OTP email notification for {}", email);
        } catch (Exception e) {
            logger.warn("Could not dispatch OTP notification to notification-service: {}", e.getMessage());
        }

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("email", email);
        resp.put("otp", otp);
        resp.put("message", "Password reset OTP sent successfully to " + email + ". Password change can also be completed through mail.");
        return ResponseEntity.ok(resp);
    }


    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUserByAdmin(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Admin creating user with email: {} and role: {}", request.getEmail(), request.getRole());
        UserResponse response = userService.createUserByAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Admin retrieving all users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
        logger.info("Admin retrieving user with id: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable("id") Long id,
                                                      @Valid @RequestBody RoleUpdateRequest request) {
        logger.info("Admin updating role for user id: {} to {}", id, request.getRole());
        UserResponse user = userService.updateUserRole(id, request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable("id") Long id,
                                                        @Valid @RequestBody StatusUpdateRequest request) {
        logger.info("Admin updating status for user id: {} to enabled={}", id, request.getEnabled());
        UserResponse user = userService.updateUserStatus(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        logger.info("Admin deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
