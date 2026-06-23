package com.upimesh.referral.repository;

import com.upimesh.referral.model.entity.ReferralRecord;
import com.upimesh.referral.model.enums.ReferralStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRecordRepository extends JpaRepository<ReferralRecord, Long> {
    Optional<ReferralRecord> findByReferralId(String referralId);
    Optional<ReferralRecord> findByRefereeId(String refereeId);
    Optional<ReferralRecord> findByRefereeIdAndStatus(String refereeId, ReferralStatus status);
    List<ReferralRecord> findByReferrerId(String referrerId);
    
    long countByIpAddress(String ipAddress);
    boolean existsByRefereeId(String refereeId);
    boolean existsByReferrerIdAndDeviceId(String referrerId, String deviceId);
    
    List<ReferralRecord> findByStatusAndRewardScheduledAtBefore(ReferralStatus status, LocalDateTime dateTime);
}
