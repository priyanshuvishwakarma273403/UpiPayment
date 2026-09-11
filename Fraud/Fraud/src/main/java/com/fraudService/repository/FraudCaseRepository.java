package com.fraudService.repository;

import com.fraudService.entity.FraudCase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudCaseRepository extends MongoRepository<FraudCase, String> {

    Optional<FraudCase> findByCaseId(String caseId);

    List<FraudCase> findByCustomerId(String customerId);

    List<FraudCase> findByTransactionId(String transactionId);

    List<FraudCase> findByStatus(String status);

    List<FraudCase> findByAssignedTo(String assignedTo);

    List<FraudCase> findByStatusAndAssignedTo(String status, String assignedTo);
}
