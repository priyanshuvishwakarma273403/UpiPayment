package com.upimesh.kyc.repository;

import com.upimesh.kyc.model.entity.KycAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KycAuditLogRepository extends JpaRepository<KycAuditLog, Long> {
    List<KycAuditLog> findByKycId(String kycId);
    List<KycAuditLog> findByUserId(String userId);
}
