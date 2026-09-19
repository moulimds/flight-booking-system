package com.flight.profile.service;

import com.flight.profile.dto.ContactUpdateRequest;
import com.flight.profile.dto.PassportUpdateRequest;
import com.flight.profile.dto.ProfileRequest;
import com.flight.profile.dto.ProfileResponse;
import com.flight.profile.entity.UserProfile;
import com.flight.profile.exception.DuplicateResourceException;
import com.flight.profile.exception.ResourceNotFoundException;
import com.flight.profile.exception.UnauthorizedException;
import com.flight.profile.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private UserProfileRepository profileRepository;

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    private boolean isSupport(String role) {
        return "SUPPORT".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    private void validateAccess(UserProfile profile, Long requestingUserId, String requestingRole) {
        if (isAdmin(requestingRole) || isSupport(requestingRole)) {
            return;
        }
        if (requestingUserId == null || !profile.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("Access denied: You are not authorized to view or modify another customer's profile");
        }
    }

    @Override
    public ProfileResponse getProfileByUserId(Long userId, Long requestingUserId, String requestingRole) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));
        validateAccess(profile, requestingUserId, requestingRole);
        return mapToResponse(profile);
    }

    @Override
    public ProfileResponse getProfileById(Long id, Long requestingUserId, String requestingRole) {
        UserProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with ID: " + id));
        validateAccess(profile, requestingUserId, requestingRole);
        return mapToResponse(profile);
    }

    @Override
    public ProfileResponse createProfile(ProfileRequest request, Long requestingUserId, String requestingRole) {
        Long targetUserId = request.getUserId();
        if (targetUserId == null) {
            targetUserId = requestingUserId;
        }

        if (targetUserId == null) {
            throw new UnauthorizedException("User ID must be provided or derived from authenticated context");
        }

        // If not admin, can only create own profile
        if (!isAdmin(requestingRole) && !targetUserId.equals(requestingUserId)) {
            throw new UnauthorizedException("Access denied: You can only create a profile for your own account");
        }

        if (profileRepository.existsByUserId(targetUserId)) {
            throw new DuplicateResourceException("Profile already exists for user ID: " + targetUserId);
        }

        UserProfile profile = new UserProfile();
        profile.setUserId(targetUserId);
        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddress(request.getAddress());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setNationality(request.getNationality());
        profile.setPassportNumber(request.getPassportNumber());
        profile.setPassportExpiryDate(request.getPassportExpiryDate());
        profile.setPassportIssuingCountry(request.getPassportIssuingCountry());
        profile.setEmergencyContactName(request.getEmergencyContactName());
        profile.setEmergencyContactPhone(request.getEmergencyContactPhone());

        UserProfile saved = profileRepository.save(profile);
        return mapToResponse(saved);
    }

    @Override
    public ProfileResponse updateProfile(Long id, ProfileRequest request, Long requestingUserId, String requestingRole) {
        UserProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with ID: " + id));
        validateAccess(profile, requestingUserId, requestingRole);

        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getEmail() != null) profile.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getNationality() != null) profile.setNationality(request.getNationality());
        if (request.getPassportNumber() != null) profile.setPassportNumber(request.getPassportNumber());
        if (request.getPassportExpiryDate() != null) profile.setPassportExpiryDate(request.getPassportExpiryDate());
        if (request.getPassportIssuingCountry() != null) profile.setPassportIssuingCountry(request.getPassportIssuingCountry());
        if (request.getEmergencyContactName() != null) profile.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null) profile.setEmergencyContactPhone(request.getEmergencyContactPhone());

        UserProfile updated = profileRepository.save(profile);
        return mapToResponse(updated);
    }

    @Override
    public ProfileResponse updatePassport(Long id, PassportUpdateRequest request, Long requestingUserId, String requestingRole) {
        UserProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with ID: " + id));
        validateAccess(profile, requestingUserId, requestingRole);

        profile.setPassportNumber(request.getPassportNumber());
        if (request.getPassportExpiryDate() != null) profile.setPassportExpiryDate(request.getPassportExpiryDate());
        if (request.getPassportIssuingCountry() != null) profile.setPassportIssuingCountry(request.getPassportIssuingCountry());

        UserProfile updated = profileRepository.save(profile);
        return mapToResponse(updated);
    }

    @Override
    public ProfileResponse updateContact(Long id, ContactUpdateRequest request, Long requestingUserId, String requestingRole) {
        UserProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with ID: " + id));
        validateAccess(profile, requestingUserId, requestingRole);

        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getEmergencyContactName() != null) profile.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null) profile.setEmergencyContactPhone(request.getEmergencyContactPhone());

        UserProfile updated = profileRepository.save(profile);
        return mapToResponse(updated);
    }

    @Override
    public List<ProfileResponse> searchProfiles(String query, String requestingRole) {
        if (!isAdmin(requestingRole) && !isSupport(requestingRole)) {
            throw new UnauthorizedException("Access denied: Only ADMIN and SUPPORT staff can search customer profiles");
        }
        return profileRepository.searchProfiles(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfileResponse> getAllProfiles(String requestingRole) {
        if (!isAdmin(requestingRole) && !isSupport(requestingRole)) {
            throw new UnauthorizedException("Access denied: Only ADMIN and SUPPORT staff can view all profiles");
        }
        return profileRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProfile(Long id, String requestingRole) {
        if (!isAdmin(requestingRole)) {
            throw new UnauthorizedException("Access denied: Only ADMIN can delete customer profiles");
        }
        UserProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with ID: " + id));
        profileRepository.delete(profile);
    }

    private ProfileResponse mapToResponse(UserProfile p) {
        ProfileResponse res = new ProfileResponse();
        res.setId(p.getId());
        res.setUserId(p.getUserId());
        res.setFullName(p.getFullName());
        res.setEmail(p.getEmail());
        res.setPhoneNumber(p.getPhoneNumber());
        res.setAddress(p.getAddress());
        res.setDateOfBirth(p.getDateOfBirth());
        res.setGender(p.getGender());
        res.setNationality(p.getNationality());
        res.setPassportNumber(p.getPassportNumber());
        res.setPassportExpiryDate(p.getPassportExpiryDate());
        res.setPassportIssuingCountry(p.getPassportIssuingCountry());
        res.setEmergencyContactName(p.getEmergencyContactName());
        res.setEmergencyContactPhone(p.getEmergencyContactPhone());
        res.setCreatedAt(p.getCreatedAt());
        res.setUpdatedAt(p.getUpdatedAt());
        return res;
    }
}
