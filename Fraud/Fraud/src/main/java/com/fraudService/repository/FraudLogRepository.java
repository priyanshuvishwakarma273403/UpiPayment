package com.fraudService.repository;

import com.fraudService.entity.FraudLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FraudLog MongoDB Repository
 */
@Repository
public interface FraudLogRepository extends MongoRepository<FraudLog, String> {

    Optional<FraudLog> findByPaymentId(String paymentId);

    List<FraudLog> findBySenderIdOrderByCheckedAtDesc(Long senderId);

    // BLOCKED fraud logs
    List<FraudLog> findByFinalDecisionAndCheckedAtAfter(String decision, LocalDateTime after);

    // High risk payments (score > 0.7)
    @Query("{ 'riskScore': { $gte: ?0 }, 'checkedAt': { $gte: ?1 } }")
    List<FraudLog> findHighRiskPayments(Double minScore, LocalDateTime since);

    // Sender ke fraud history
    long countBySenderIdAndFinalDecisionAndCheckedAtAfter(
            Long senderId, String decision, LocalDateTime after);

    // Repeated receiver check
    @Query("{ 'senderId': ?0, 'receiverUpiId': ?1, 'amount': ?2, 'checkedAt': { $gte: ?3 } }")
    List<FraudLog> findRepeatedTransactions(
            Long senderId, String receiverUpiId,
            java.math.BigDecimal amount, LocalDateTime since);
}
