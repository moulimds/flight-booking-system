package com.flight.profile.service;

import com.flight.profile.dto.ContactUpdateRequest;
import com.flight.profile.dto.PassportUpdateRequest;
import com.flight.profile.dto.ProfileRequest;
import com.flight.profile.dto.ProfileResponse;

import java.util.List;

public interface ProfileService {

    ProfileResponse getProfileByUserId(Long userId, Long requestingUserId, String requestingRole);

    ProfileResponse getProfileById(Long id, Long requestingUserId, String requestingRole);

    ProfileResponse createProfile(ProfileRequest request, Long requestingUserId, String requestingRole);

    ProfileResponse updateProfile(Long id, ProfileRequest request, Long requestingUserId, String requestingRole);

    ProfileResponse updatePassport(Long id, PassportUpdateRequest request, Long requestingUserId, String requestingRole);

    ProfileResponse updateContact(Long id, ContactUpdateRequest request, Long requestingUserId, String requestingRole);

    List<ProfileResponse> searchProfiles(String query, String requestingRole);

    List<ProfileResponse> getAllProfiles(String requestingRole);

    void deleteProfile(Long id, String requestingRole);
}
