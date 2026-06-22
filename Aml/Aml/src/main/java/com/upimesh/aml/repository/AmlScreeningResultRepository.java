package com.upimesh.aml.repository;

import com.upimesh.aml.model.entity.AmlScreeningResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AmlScreeningResultRepository extends JpaRepository<AmlScreeningResult, Long> {
    Optional<AmlScreeningResult> findByTransactionId(String transactionId);
    List<AmlScreeningResult> findByUserUpiIdAndScreenedAtAfter(String userUpiId, LocalDateTime screenedAt);
    long countByUserUpiIdAndScreenedAtAfter(String userUpiId, LocalDateTime screenedAt);
}
