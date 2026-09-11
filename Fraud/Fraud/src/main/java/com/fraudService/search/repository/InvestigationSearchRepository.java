package com.fraudService.search.repository;

import com.fraudService.search.model.InvestigationSearchDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestigationSearchRepository extends MongoRepository<InvestigationSearchDocument, String> {

    Optional<InvestigationSearchDocument> findByDocId(String docId);

    List<InvestigationSearchDocument> findByIndexingStatus(String indexingStatus);

    List<InvestigationSearchDocument> findByTransactionId(String transactionId);

    List<InvestigationSearchDocument> findByCustomerId(String customerId);

    List<InvestigationSearchDocument> findByCaseId(String caseId);
}
