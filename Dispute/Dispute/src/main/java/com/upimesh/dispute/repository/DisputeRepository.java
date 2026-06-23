package com.upimesh.dispute.repository;

import com.upimesh.dispute.model.entity.Dispute;
import com.upimesh.dispute.model.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    Optional<Dispute> findByDisputeId(String disputeId);
    Optional<Dispute> findByTransactionId(String transactionId);
    List<Dispute> findByUserId(String userId);
    List<Dispute> findByUserUpiId(String userUpiId);
    List<Dispute> findByMerchantUpiId(String merchantUpiId);
    List<Dispute> findByStatusAndMerchantResponseDeadlineLessThanEqual(DisputeStatus status, LocalDateTime deadline);
}
