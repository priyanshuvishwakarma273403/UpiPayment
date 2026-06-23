package com.upimesh.risk.repository;

import com.upimesh.risk.model.entity.RiskScoringResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskScoringResultRepository extends JpaRepository<RiskScoringResult, Long> {
    Optional<RiskScoringResult> findByTransactionId(String transactionId);
    List<RiskScoringResult> findByUserIdOrderByScoredAtDesc(String userId);
}
