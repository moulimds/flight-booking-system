package com.flight.profile.repository;

import com.flight.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByUserId(Long userId);

    @Query("SELECT p FROM UserProfile p WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%')) OR p.passportNumber LIKE CONCAT('%', :query, '%')")
    List<UserProfile> searchProfiles(@Param("query") String query);
}
