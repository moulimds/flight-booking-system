package com.example.fare.repository;

import com.example.fare.entity.FareRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareRuleRepository extends JpaRepository<FareRule, Long> {
    Optional<FareRule> findByFareId(Long fareId);
}
