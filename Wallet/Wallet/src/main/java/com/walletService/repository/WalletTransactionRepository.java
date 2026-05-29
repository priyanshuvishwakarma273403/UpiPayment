package com.walletService.repository;

import com.walletService.entity.WalletTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction , Long> {

    // Paginated transactions for a user
    List<WalletTransaction> findByUserIdOrderByTransactionTimeDesc(Long userId, Pageable pageable);
    // Date range ke transactions
    List<WalletTransaction> findByUserIdAndTransactionTimeBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end);

    // Ek din mein kitna debit hua (daily limit check ke liye)
    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt " +
            "WHERE wt.userId = :userId AND wt.transactionType = 'DEBIT' " +
            "AND wt.transactionTime >= :startOfDay")
    BigDecimal getTodayDebitTotal(Long userId, LocalDateTime startOfDay);

}
