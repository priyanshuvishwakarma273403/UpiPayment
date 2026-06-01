package com.rewards_service.Rewards.repository;

import com.rewards_service.Rewards.entity.RewardTransaction;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {

    Page<RewardTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByReferenceIdAndTransactionType(String refId, RewardTransaction.RewardTxnType type);

    // Points expiring soon (next 30 days)
    @Query("SELECT t FROM RewardTransaction t WHERE t.userId = :userId " +
            "AND t.expiryDate BETWEEN :now AND :future " +
            "AND t.transactionType IN ('EARNED_PAYMENT','EARNED_REFERRAL','EARNED_OFFER')")
    List<RewardTransaction> findExpiringSoonPoints(Long userId, LocalDateTime now, LocalDateTime future);

}
