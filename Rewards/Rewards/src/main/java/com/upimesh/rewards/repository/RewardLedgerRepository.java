package com.upimesh.rewards.repository;

import com.upimesh.rewards.model.entity.RewardLedger;
import com.upimesh.rewards.model.enums.RewardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardLedgerRepository extends JpaRepository<RewardLedger, Long> {
    Optional<RewardLedger> findByLedgerId(String ledgerId);
    Optional<RewardLedger> findByTransactionId(String transactionId);
    Page<RewardLedger> findByUserId(String userId, Pageable pageable);
    List<RewardLedger> findByStatusAndExpiresAtBefore(RewardStatus status, LocalDateTime dateTime);
}
