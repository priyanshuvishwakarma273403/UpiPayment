package com.transaction_service.repository.mysql;

import com.transaction_service.entity.mysql.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    // User ki transaction history (paginated)
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Merchant ki transactions
    Page<Transaction> findByMerchantIdOrderByCreatedAtDesc(Long merchantId, Pageable pageable);

    // Payment ke saare transactions (debit + credit)
    List<Transaction> findByPaymentId(String paymentId);

    // Date range query
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
            "AND t.createdAt BETWEEN :from AND :to ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndDateRange(Long userId, LocalDateTime from, LocalDateTime to);

    // Total spend in a period (for expense insights)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.userId = :userId AND t.transactionType = 'DEBIT' " +
            "AND t.status = 'SUCCESS' AND t.createdAt >= :since")
    BigDecimal getTotalDebitSince(Long userId, LocalDateTime since);

    // Merchant settlement: total received
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.merchantId = :merchantId AND t.transactionType = 'CREDIT' " +
            "AND t.status = 'SUCCESS' AND t.createdAt BETWEEN :from AND :to")
    BigDecimal getMerchantSettlementAmount(Long merchantId, LocalDateTime from, LocalDateTime to);

    // Reconciliation: payments without transactions
    @Query("SELECT DISTINCT t.paymentId FROM Transaction t WHERE t.paymentId IN :paymentIds")
    List<String> findExistingPaymentIds(List<String> paymentIds);

    // Fetch only DEBIT transactions within a specific timestamp range
    @Query("SELECT t FROM Transaction t WHERE t.transactionType = 'DEBIT' AND t.createdAt BETWEEN :from AND :to")
    List<Transaction> findDebitTransactionsBetween(org.springframework.data.repository.query.Param("from") LocalDateTime from, org.springframework.data.repository.query.Param("to") LocalDateTime to);

}
