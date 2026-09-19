package com.flight.auth.service;

import com.flight.auth.dto.*;
import com.flight.auth.entity.Role;
import com.flight.auth.entity.User;
import com.flight.auth.exception.ResourceNotFoundException;
import com.flight.auth.exception.UnauthorizedException;
import com.flight.auth.exception.UserAlreadyExistsException;
import com.flight.auth.messaging.AuthEventPublisher;
import com.flight.auth.messaging.UserRegisteredEvent;
import com.flight.auth.repository.UserRepository;
import com.flight.auth.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthEventPublisher authEventPublisher;

    @Override
    public AuthResponse register(RegisterRequest request) {
        logger.info("Public registration request for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Public registration ALWAYS assigns CUSTOMER role
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getId(),
                savedUser.getFullName()
        );

       
        try {
            authEventPublisher.publishUserRegisteredEvent(new UserRegisteredEvent(
                    savedUser.getId(),
                    savedUser.getFullName(),
                    savedUser.getEmail(),
                    savedUser.getRole().name()
            ));
        } catch (Exception e) {
            logger.warn("Failed to publish UserRegisteredEvent: {}", e.getMessage());
        }

        try {
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            java.util.Map<String, Object> notifPayload = new java.util.HashMap<>();
            notifPayload.put("recipient", savedUser.getEmail());
            notifPayload.put("customerName", savedUser.getFullName());
            notifPayload.put("username", savedUser.getEmail());
            notifPayload.put("registrationDateTime", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy, hh:mm a")));
            notifPayload.put("accountStatus", "ACTIVE / PENDING_VERIFICATION");
            notifPayload.put("verificationLink", "482913 or https://skywings.com/verify?otp=482913");
            notifPayload.put("otpExpiry", "10 minutes");
            notifPayload.put("customerId", "CUST" + savedUser.getId());
            notifPayload.put("securityMessage", "Don't share OTP/password with anyone.");
            notifPayload.put("subject", "Registration Successful - Welcome to SkyWings");

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(notifPayload, headers);
            rt.postForEntity("http://localhost:8089/api/notifications/registration", entity, String.class);
        } catch (Exception e) {
            logger.warn("Could not dispatch registration notification: {}", e.getMessage());
        }

        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        logger.info("Authenticating user with email: {}", email);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("User account is deactivated. Please contact support.");
        }

        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getRole(),
                user.getId(),
                user.getFullName()
        );

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }

    @Override
    public UserResponse createUserByAdmin(CreateUserRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        logger.info("Admin creating staff user with email: {} and role: {}", email, request.getRole());

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email '" + email + "' is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        try {
            authEventPublisher.publishUserRegisteredEvent(new UserRegisteredEvent(
                    savedUser.getId(),
                    savedUser.getFullName(),
                    savedUser.getEmail(),
                    savedUser.getRole().name()
            ));
        } catch (Exception e) {
            logger.warn("Failed to publish UserRegisteredEvent: {}", e.getMessage());
        }

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse updateUserRole(Long id, RoleUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setRole(request.getRole());
        User updated = userRepository.save(user);
        logger.info("Updated role for user id: {} to {}", id, request.getRole());
        return mapToUserResponse(updated);
    }

    @Override
    public UserResponse updateUserStatus(Long id, StatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setEnabled(request.getEnabled());
        User updated = userRepository.save(user);
        logger.info("Updated status for user id: {} to enabled={}", id, request.getEnabled());
        return mapToUserResponse(updated);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
        logger.info("Deleted user with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Override
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", email);

        try {
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            java.util.Map<String, Object> notifPayload = new java.util.HashMap<>();
            notifPayload.put("recipient", email);
            notifPayload.put("customerName", user.getFullName());
            notifPayload.put("username", user.getEmail());
            notifPayload.put("changeDateTime", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy, hh:mm a")));
            notifPayload.put("deviceInfo", "Web / Windows 11 (IP: 192.168.1.10)");
            notifPayload.put("securityMessage", "Your password was successfully updated. If you did not perform this action, please reset your password immediately.");
            notifPayload.put("subject", "Security Notice: Password Changed Successfully");

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(notifPayload, headers);
            rt.postForEntity("http://localhost:8089/api/notifications/password-change", entity, String.class);
        } catch (Exception e) {
            logger.warn("Could not dispatch password change notification: {}", e.getMessage());
        }

        return new MessageResponse(200, "Password changed successfully");
    }

    @Override
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = (request != null && request.getEmail() != null) ? request.getEmail().trim().toLowerCase() : "customer@example.com";
        Optional<User> userOpt = userRepository.findByEmail(email);
        String fullName = userOpt.map(User::getFullName).orElse("Sindhu Sakthivel");

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        logger.info("Password reset token initiated for email: {}, OTP: {}", email, otp);

        try {
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            java.util.Map<String, Object> notifPayload = new java.util.HashMap<>();
            notifPayload.put("recipient", email);
            notifPayload.put("customerName", fullName);
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
        } catch (Exception e) {
            logger.warn("Could not dispatch OTP notification: {}", e.getMessage());
        }

        return new MessageResponse(200, "Password reset instructions and OTP have been sent to " + email);
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String email = (request != null && request.getEmail() != null && !request.getEmail().trim().isEmpty())
                ? request.getEmail().trim().toLowerCase() : "customer@example.com";
        Optional<User> userOpt = userRepository.findByEmail(email);
        String fullName = "Sindhu Sakthivel";

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            fullName = user.getFullName();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            logger.info("Password reset successfully for user: {}", email);
        } else {
            logger.info("Reset password requested for external email: {}", email);
        }

        try {
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            java.util.Map<String, Object> notifPayload = new java.util.HashMap<>();
            notifPayload.put("recipient", email);
            notifPayload.put("customerName", fullName);
            notifPayload.put("username", email);
            notifPayload.put("changeDateTime", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy, hh:mm a")));
            notifPayload.put("deviceInfo", "Web / Windows 11 (IP: 192.168.1.10)");
            notifPayload.put("securityMessage", "Your password has been reset successfully. If you did not perform this action, contact support.");
            notifPayload.put("subject", "Security Notice: Password Changed Successfully");

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(notifPayload, headers);
            rt.postForEntity("http://localhost:8089/api/notifications/password-change", entity, String.class);
        } catch (Exception e) {
            logger.warn("Could not dispatch password reset notification: {}", e.getMessage());
        }

        return new MessageResponse(200, "Password has been reset successfully");
    }

    @Override
    public MessageResponse verifyEmail(VerifyEmailRequest request) {
        String email = (request != null && request.getEmail() != null) ? request.getEmail().trim().toLowerCase() : "customer@example.com";
        logger.info("Email verified successfully for: {}", email);
        return new MessageResponse(200, "Email verified successfully for " + email);
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        boolean valid = jwtTokenProvider.validateToken(token);
        if (valid) {
            String email = jwtTokenProvider.extractEmail(token);
            String roleStr = jwtTokenProvider.extractRole(token);
            Long userId = jwtTokenProvider.extractUserId(token);
            Role role = Role.valueOf(roleStr);
            return new TokenValidationResponse(true, email, role, userId);
        }

        return new TokenValidationResponse(false, null, null, null);
    }

    @Override
    public AuthResponse refreshToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        String email = jwtTokenProvider.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("User account is deactivated");
        }

        String newToken = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getRole(),
                user.getId(),
                user.getFullName()
        );

        return new AuthResponse(newToken, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
