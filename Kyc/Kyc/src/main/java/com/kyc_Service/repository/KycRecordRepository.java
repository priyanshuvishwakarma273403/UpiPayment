package com.kyc_Service.repository;

import com.kyc_Service.entity.KycLevel;
import com.kyc_Service.entity.KycRecord;
import com.kyc_Service.entity.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// kycRecord Repository - mysql
public interface KycRecordRepository extends JpaRepository<KycRecord,Long> {

    Optional<KycRecord> findByUserId(Long userId);

    Optional<KycRecord> findByPanNumber(String panNumber);

    Optional<KycRecord> findByAadhaarNumber(String aadhaarNumber);

    boolean existsByUserId(Long userId);

    boolean existsByPanNumber(String panNumber);

    boolean existsByAadhaarNumber(String aadhaarNumber);

    List<KycRecord> findByKycStatus(KycStatus status);

    // Expired KYC records (re-verification needed)
    @Query("SELECT k FROM KycRecord k WHERE k.expiresAt < :now AND k.kycStatus = 'COMPLETED'")
    List<KycRecord> findExpiredKycRecords(LocalDateTime now);

    // Stats
    long countByKycLevel(KycLevel level);

    long countByKycStatus(KycStatus status);


}
