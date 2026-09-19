package com.flight.auth.service;

import com.flight.auth.dto.*;

import java.util.List;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse createUserByAdmin(CreateUserRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUserRole(Long id, RoleUpdateRequest request);

    UserResponse updateUserStatus(Long id, StatusUpdateRequest request);

    void deleteUser(Long id);

    UserResponse getCurrentUserProfile(String email);

    MessageResponse changePassword(String email, ChangePasswordRequest request);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    MessageResponse verifyEmail(VerifyEmailRequest request);

    TokenValidationResponse validateToken(String token);

    AuthResponse refreshToken(String token);
}
