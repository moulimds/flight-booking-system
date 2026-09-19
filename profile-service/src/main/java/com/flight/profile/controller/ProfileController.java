package com.flight.profile.controller;

import com.flight.profile.dto.ContactUpdateRequest;
import com.flight.profile.dto.PassportUpdateRequest;
import com.flight.profile.dto.ProfileRequest;
import com.flight.profile.dto.ProfileResponse;
import com.flight.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/profiles", "/profiles"})
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "CUSTOMER") String requestingRole) {
        ProfileResponse response = profileService.getProfileByUserId(requestingUserId, requestingUserId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfileById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "CUSTOMER") String requestingRole) {
        ProfileResponse response = profileService.getProfileById(id, requestingUserId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "CUSTOMER") String requestingRole) {
        ProfileResponse response = profileService.getProfileByUserId(userId, requestingUserId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            @Valid @RequestBody ProfileRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "CUSTOMER") String requestingRole) {
        ProfileResponse response = profileService.createProfile(request, requestingUserId, requestingRole);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody ProfileRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "CUSTOMER") String requestingRole) {
        ProfileResponse response = profileService.updateProfile(id, request, requestingUserId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/passport")
    public ResponseEntity<ProfileResponse> updatePassport(
            @PathVariable Long id,
            @Valid @RequestBody PassportUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "CUSTOMER") String requestingRole) {
        ProfileResponse response = profileService.updatePassport(id, request, requestingUserId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/contact")
    public ResponseEntity<ProfileResponse> updateContact(
            @PathVariable Long id,
            @RequestBody ContactUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long requestingUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "CUSTOMER") String requestingRole) {
        ProfileResponse response = profileService.updateContact(id, request, requestingUserId, requestingRole);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProfileResponse>> searchProfiles(
            @RequestParam String query,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "ADMIN") String requestingRole) {
        List<ProfileResponse> results = profileService.searchProfiles(query, requestingRole);
        return ResponseEntity.ok(results);
    }

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getAllProfiles(
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "ADMIN") String requestingRole) {
        List<ProfileResponse> list = profileService.getAllProfiles(requestingRole);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProfile(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "ADMIN") String requestingRole) {
        profileService.deleteProfile(id, requestingRole);
        return ResponseEntity.ok(Map.of("message", "Profile deleted successfully", "profileId", String.valueOf(id)));
    }
}
