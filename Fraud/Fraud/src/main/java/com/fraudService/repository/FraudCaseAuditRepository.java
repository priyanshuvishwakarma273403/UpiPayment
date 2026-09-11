package com.fraudService.repository;

import com.fraudService.entity.FraudCaseAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudCaseAuditRepository extends MongoRepository<FraudCaseAudit, String> {

    List<FraudCaseAudit> findByCaseIdOrderByTimestampDesc(String caseId);
}
