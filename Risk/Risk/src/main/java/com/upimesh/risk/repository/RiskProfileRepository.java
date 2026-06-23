package com.upimesh.risk.repository;

import com.upimesh.risk.model.entity.RiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskProfileRepository extends JpaRepository<RiskProfile, Long> {
    Optional<RiskProfile> findByUserId(String userId);
    Optional<RiskProfile> findByUserUpiId(String userUpiId);
}
