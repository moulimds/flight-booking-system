package com.flightbooking.auth.service;

import com.flightbooking.auth.dto.*;
import com.flightbooking.auth.model.Role;
import com.flightbooking.auth.model.User;
import com.flightbooking.auth.repository.UserRepository;
import com.flightbooking.auth.security.JwtService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository repo, PasswordEncoder encoder, JwtService jwtService) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest r) {
        if (repo.existsByEmailIgnoreCase(r.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        User u = new User();
        u.setFullName(r.fullName());
        u.setEmail(r.email().toLowerCase());
        u.setUsername(r.email().toLowerCase());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(Role.CUSTOMER);
        u.setActive(true);
        u.setEmailVerified(false);
        return repo.save(u);
    }

    public User adminRegister(AdminRegisterRequest r) {
        if (repo.existsByEmailIgnoreCase(r.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        User u = new User();
        u.setFullName(r.fullName());
        u.setEmail(r.email().toLowerCase());
        u.setUsername(r.email().toLowerCase());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(r.role());
        u.setActive(true);
        u.setEmailVerified(true);
        return repo.save(u);
    }

    public String login(LoginRequest r) {
        User u = repo.findByEmailIgnoreCase(r.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!u.isActive())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated. Contact admin.");
        if (!encoder.matches(r.password(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        return jwtService.generate(u);
    }

    public User currentUser(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<User> allUsers() { return repo.findAll(); }

    public void changePassword(Long id, ChangePasswordRequest r) {
        User u = currentUser(id);
        if (!encoder.matches(r.oldPassword(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        u.setPasswordHash(encoder.encode(r.newPassword()));
        repo.save(u);
    }

    public User changeRole(Long id, Role role) {
        User u = currentUser(id);
        u.setRole(role);
        return repo.save(u);
    }

    public void deleteUser(Long id) {
        User u = currentUser(id);
        repo.delete(u);
    }

    public void verifyEmail(Long id) {
        User u = currentUser(id);
        u.setEmailVerified(true);
        repo.save(u);
    }

    public String forgotPassword(String email) {
        User u = repo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this email not found"));
        String otp = String.format("%06d", new Random().nextInt(1000000));
        u.setResetOtp(otp);
        u.setResetOtpExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
        repo.save(u);
        return otp;
    }

    public void resetPassword(ResetPasswordRequest r) {
        User u = repo.findByEmailIgnoreCase(r.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request or user not found"));
        if (u.getResetOtp() == null || !u.getResetOtp().equals(r.otp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }
        if (u.getResetOtpExpiry() == null || Instant.now().isAfter(u.getResetOtpExpiry())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }
        u.setPasswordHash(encoder.encode(r.newPassword()));
        u.setResetOtp(null);
        u.setResetOtpExpiry(null);
        repo.save(u);
    }
}
