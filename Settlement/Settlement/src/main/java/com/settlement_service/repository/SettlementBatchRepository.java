package com.settlement_service.repository;

import com.settlement_service.entity.SettlementBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementBatchRepository extends JpaRepository<SettlementBatch,Long> {

    Optional<SettlementBatch> findByBatchId(String batchId);

    Page<SettlementBatch> findByMerchantIdOrderBySettlementDateDesc(Long merchantId, Pageable pageable);

    List<SettlementBatch> findByStatusOrderByCreatedAtAsc(SettlementBatch.SettlementStatus status);

    List<SettlementBatch> findBySettlementDateAndStatus(LocalDate date, SettlementBatch.SettlementStatus status);

    boolean existsByMerchantIdAndSettlementDate(Long merchantId, LocalDate date);

    @Query("SELECT COALESCE(SUM(s.netAmount),0) FROM SettlementBatch s " +
            "WHERE s.merchantId = :merchantId AND s.status = 'COMPLETED' " +
            "AND s.settlementDate BETWEEN :from AND :to")
    BigDecimal getTotalSettledAmount(Long merchantId, LocalDate from, LocalDate to);

    @Query("SELECT COUNT(s) FROM SettlementBatch s WHERE s.status = 'PENDING' AND s.retryCount < 3")
    long countPendingSettlements();

}
