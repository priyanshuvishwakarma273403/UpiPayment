package com.upimesh.kyc.repository;

import com.upimesh.kyc.model.entity.KycRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycRecordRepository extends JpaRepository<KycRecord, Long> {
    Optional<KycRecord> findByUserId(String userId);
    Optional<KycRecord> findByUserUpiId(String userUpiId);
    Optional<KycRecord> findByKycId(String kycId);
    boolean existsByUserId(String userId);
    boolean existsByUserUpiId(String userUpiId);
}
