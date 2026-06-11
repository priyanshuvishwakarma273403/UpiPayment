package com.npci.repository;

import com.npci.model.entity.UpiTransaction;
import com.npci.model.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UpiTransactionRepository extends JpaRepository<UpiTransaction, Long> {
    Optional<UpiTransaction> findByTransactionId(String transactionId);

    Optional<UpiTransaction> findByNpciTransactionId(String npciTransactionId);

    // Idempotency check — prevent duplicate payment if same request sent twice
    @Query("SELECT t FROM UpiTransaction t WHERE t.senderUpiId = :sender " +
            "AND t.receiverUpiId = :receiver AND t.amount = :amount " +
            "AND t.createdAt > :since AND t.status NOT IN ('FAILED', 'TIMEOUT')")
    Optional<UpiTransaction> findRecentDuplicate(
            @Param("sender") String senderUpiId,
            @Param("receiver") String receiverUpiId,
            @Param("amount") BigDecimal amount,
            @Param("since") LocalDateTime since
    );

    // Find all pending transactions older than X minutes (for timeout handling)
    List<UpiTransaction> findByStatusAndCreatedAtBefore(
            TransactionStatus status, LocalDateTime before);

    // Daily total for a user (for limit enforcement)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM UpiTransaction t " +
            "WHERE t.senderUpiId = :upiId AND t.status = 'SUCCESS' " +
            "AND t.createdAt >= :dayStart")
    BigDecimal getDailyTotal(
            @Param("upiId") String upiId,
            @Param("dayStart") LocalDateTime dayStart
    );

    List<UpiTransaction> findBySenderUpiIdOrderByCreatedAtDesc(String senderUpiId);

    List<UpiTransaction> findByStatusAndRetryCountLessThan(TransactionStatus status, int maxRetries);

}
