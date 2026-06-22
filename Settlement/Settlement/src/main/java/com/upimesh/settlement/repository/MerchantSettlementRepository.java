package com.upimesh.settlement.repository;

import com.upimesh.settlement.model.entity.MerchantSettlement;
import com.upimesh.settlement.model.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantSettlementRepository extends JpaRepository<MerchantSettlement, Long> {

    Optional<MerchantSettlement> findBySettlementId(String settlementId);

    List<MerchantSettlement> findByBatchId(String batchId);

    List<MerchantSettlement> findByMerchantUpiIdOrderByCreatedAtDesc(String merchantUpiId);

    List<MerchantSettlement> findByStatusAndRetryCountLessThan(SettlementStatus status, int maxRetryCount);

    List<MerchantSettlement> findByBatchIdAndStatus(String batchId, SettlementStatus status);
}
