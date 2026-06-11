package com.npci.repository;

import com.npci.model.entity.RefundRecord;
import com.npci.model.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRecordRepository extends JpaRepository<RefundRecord, Long> {

    Optional<RefundRecord> findByRefundId(String refundId);

    Optional<RefundRecord> findByOriginalTransactionId(String originalTransactionId);

    // Find failed refunds to retry (max 3 retries)
    List<RefundRecord> findByStatusAndRetryCountLessThan(RefundStatus status, int maxRetries);

}
