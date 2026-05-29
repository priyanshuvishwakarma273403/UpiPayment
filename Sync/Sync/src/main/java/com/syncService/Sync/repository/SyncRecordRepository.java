package com.syncService.Sync.repository;

import com.syncService.Sync.entity.SyncRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SyncRecordRepository extends JpaRepository<SyncRecord, Integer> {

    // Specific user ke pending records
    List<SyncRecord> findBySenderIdAndStatusOrderByCreatedAtAsc(
            Long senderId, SyncRecord.SyncStatus status);

    // Saare PENDING records jinka retry count max se kam hai
    List<SyncRecord> findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            SyncRecord.SyncStatus status, int maxRetry);

    // FAILED records jo retry eligible hain
    List<SyncRecord> findByStatusAndRetryCountLessThan(
            SyncRecord.SyncStatus status, int maxRetry);

    // Retry time aa gayi hai jo records
    @Query("SELECT s FROM SyncRecord s WHERE s.status = 'PENDING' " +
            "AND s.nextRetryAt <= :now ORDER BY s.nextRetryAt ASC")
    List<SyncRecord> findDueForSync(LocalDateTime now);

    Optional<SyncRecord> findByPaymentId(String paymentId);

    boolean existsByPaymentId(String paymentId);

    // Stats ke liye
    long countByStatus(SyncRecord.SyncStatus status);

    long countBySenderIdAndStatus(Long senderId, SyncRecord.SyncStatus status);

}
