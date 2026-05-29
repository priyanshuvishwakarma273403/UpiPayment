package com.paymentService.repository;

import com.paymentService.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Repository - MySQL ke saath kaam karta hai
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    // Sender ke saare payments (paginated)
    Page<Payment> findBySenderIdOrderByCreatedAtDesc(Long senderId, Pageable pageable);

    // Receiver ke saare payments
    Page<Payment> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    // Idempotency check
    boolean existsByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    // Offline pending payments (sync ke liye)
    List<Payment> findByPaymentStatusOrderByCreatedAtAsc(Payment.PaymentStatus status);

    // Date range ke payments
    @Query("SELECT p FROM Payment p WHERE p.senderId = :userId " +
            "AND p.createdAt BETWEEN :start AND :end " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findUserPaymentsByDateRange(Long userId, LocalDateTime start, LocalDateTime end);

    // Status update (bulk update for sync)
    @Modifying
    @Query("UPDATE Payment p SET p.paymentStatus = :newStatus WHERE p.paymentId IN :paymentIds")
    int bulkUpdateStatus(List<String> paymentIds, Payment.PaymentStatus newStatus);

    // Failed payments count (monitoring)
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = 'FAILED' AND p.createdAt >= :since")
    long countFailedPaymentsSince(LocalDateTime since);

    // Pending sync payments (offline queue)
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PENDING_SYNC' " +
            "AND p.senderId = :senderId ORDER BY p.queuedAt ASC")
    List<Payment> findPendingSyncPaymentsBySender(Long senderId);
}
