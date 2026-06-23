package com.upimesh.referral.repository;

import com.upimesh.referral.model.entity.ReferralCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralCodeRepository extends JpaRepository<ReferralCode, Long> {
    Optional<ReferralCode> findByUserId(String userId);
    Optional<ReferralCode> findByCode(String code);
    boolean existsByCode(String code);
    List<ReferralCode> findTop10ByOrderByTotalReferralsDesc();
}
