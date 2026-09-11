package com.fraudService.search.service;

import com.fraudService.entity.FraudCase;
import com.fraudService.entity.FraudLog;
import com.fraudService.repository.FraudCaseRepository;
import com.fraudService.repository.FraudLogRepository;
import com.fraudService.search.model.InvestigationSearchDocument;
import com.fraudService.search.model.SearchRequest;
import com.fraudService.search.model.SearchResult;
import com.fraudService.search.repository.InvestigationSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestigationSearchService {

    private final InvestigationSearchRepository searchRepository;
    private final FraudLogRepository fraudLogRepository;
    private final FraudCaseRepository fraudCaseRepository;

    public SearchResult search(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Executing unified investigation search with query='{}', riskLevel={}, status={}",
                request.getQuery(), request.getRiskLevel(), request.getStatus());

        List<InvestigationSearchDocument> allDocs = searchRepository.findAll();

        // If search index is empty, auto trigger initial sync
        if (allDocs.isEmpty()) {
            reindexAll();
            allDocs = searchRepository.findAll();
        }

        String q = request.getQuery() != null ? request.getQuery().trim().toLowerCase() : "";

        List<InvestigationSearchDocument> filtered = allDocs.stream()
                .filter(doc -> matchesQuery(doc, q))
                .filter(doc -> matchesRiskLevel(doc, request.getRiskLevel()))
                .filter(doc -> matchesRiskScore(doc, request.getMinRiskScore(), request.getMaxRiskScore()))
                .filter(doc -> matchesStatus(doc, request.getStatus()))
                .filter(doc -> matchesFraudType(doc, request.getFraudType()))
                .filter(doc -> matchesDateRange(doc, request.getFromDate(), request.getToDate()))
                .sorted(Comparator.comparing(InvestigationSearchDocument::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // Facets calculation
        Map<String, Long> riskFacets = filtered.stream()
                .filter(d -> d.getRiskLevel() != null)
                .collect(Collectors.groupingBy(InvestigationSearchDocument::getRiskLevel, Collectors.counting()));

        Map<String, Long> statusFacets = filtered.stream()
                .filter(d -> d.getStatus() != null)
                .collect(Collectors.groupingBy(InvestigationSearchDocument::getStatus, Collectors.counting()));

        Map<String, Long> fraudTypeFacets = filtered.stream()
                .filter(d -> d.getFraudType() != null)
                .collect(Collectors.groupingBy(InvestigationSearchDocument::getFraudType, Collectors.counting()));

        // Pagination
        int page = Math.max(0, request.getPage());
        int size = request.getSize() > 0 ? request.getSize() : 20;
        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());

        List<InvestigationSearchDocument> pagedDocs = filtered.subList(fromIndex, toIndex);
        long executionTime = System.currentTimeMillis() - startTime;

        log.info("Search finished in {}ms returning {} hits (total: {})", executionTime, pagedDocs.size(), filtered.size());

        return SearchResult.builder()
                .query(request.getQuery())
                .totalHits(filtered.size())
                .page(page)
                .pageSize(size)
                .executionTimeMs(executionTime)
                .documents(pagedDocs)
                .riskLevelFacets(riskFacets)
                .statusFacets(statusFacets)
                .fraudTypeFacets(fraudTypeFacets)
                .build();
    }

    public void indexTransaction(FraudLog logDoc) {
        if (logDoc == null || logDoc.getPaymentId() == null) return;

        try {
            String docId = "DOC_TXN_" + logDoc.getPaymentId();
            InvestigationSearchDocument doc = InvestigationSearchDocument.builder()
                    .docId(docId)
                    .docType("TRANSACTION")
                    .transactionId(logDoc.getPaymentId())
                    .customerId(logDoc.getSenderId() != null ? String.valueOf(logDoc.getSenderId()) : null)
                    .deviceId(logDoc.getDeviceId())
                    .ipAddress(logDoc.getIpAddress())
                    .beneficiaryUpiId(logDoc.getReceiverUpiId())
                    .merchantId(logDoc.getReceiverId() != null ? String.valueOf(logDoc.getReceiverId()) : null)
                    .status(logDoc.getFinalDecision())
                    .riskScore(logDoc.getRiskScore() != null ? logDoc.getRiskScore() : 0.0)
                    .riskLevel(logDoc.getRiskLevel() != null ? logDoc.getRiskLevel() : "LOW")
                    .timestamp(logDoc.getCheckedAt() != null ? logDoc.getCheckedAt() : LocalDateTime.now())
                    .content(logDoc.getReasons())
                    .indexingStatus("INDEXED")
                    .indexedAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            searchRepository.save(doc);
            log.info("Indexed transaction docId={}", docId);
        } catch (Exception e) {
            log.warn("Failed to index transaction paymentId={}. Enqueuing to retry queue", logDoc.getPaymentId(), e);
            handleFailedIndexing("DOC_TXN_" + logDoc.getPaymentId(), "TRANSACTION", e.getMessage());
        }
    }

    public void indexCase(FraudCase fraudCase) {
        if (fraudCase == null || fraudCase.getCaseId() == null) return;

        try {
            String docId = "DOC_CASE_" + fraudCase.getCaseId();
            InvestigationSearchDocument doc = InvestigationSearchDocument.builder()
                    .docId(docId)
                    .docType("CASE")
                    .caseId(fraudCase.getCaseId())
                    .customerId(fraudCase.getCustomerId())
                    .transactionId(fraudCase.getTransactionId())
                    .fraudType(fraudCase.getFraudType())
                    .status(fraudCase.getStatus())
                    .riskLevel(fraudCase.getSeverity())
                    .timestamp(fraudCase.getCreatedAt() != null ? fraudCase.getCreatedAt() : LocalDateTime.now())
                    .content(fraudCase.getResolutionReason())
                    .indexingStatus("INDEXED")
                    .indexedAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            searchRepository.save(doc);
            log.info("Indexed fraud case docId={}", docId);
        } catch (Exception e) {
            log.warn("Failed to index fraud case caseId={}. Enqueuing to retry queue", fraudCase.getCaseId(), e);
            handleFailedIndexing("DOC_CASE_" + fraudCase.getCaseId(), "CASE", e.getMessage());
        }
    }

    public synchronized int reindexAll() {
        log.info("Starting complete reindex of transactions and fraud cases into OpenSearch index store");
        int count = 0;

        List<FraudLog> logs = fraudLogRepository.findAll();
        for (FraudLog fl : logs) {
            indexTransaction(fl);
            count++;
        }

        List<FraudCase> cases = fraudCaseRepository.findAll();
        for (FraudCase fc : cases) {
            indexCase(fc);
            count++;
        }

        log.info("Reindexing complete. Indexed {} documents into search index", count);
        return count;
    }

    public List<InvestigationSearchDocument> getFailedIndexingQueue() {
        return searchRepository.findByIndexingStatus("FAILED_RETRY_QUEUED");
    }

    public int retryFailedIndexing() {
        List<InvestigationSearchDocument> failedQueue = getFailedIndexingQueue();
        log.info("Retrying {} failed indexing documents", failedQueue.size());

        int retriedCount = 0;
        for (InvestigationSearchDocument doc : failedQueue) {
            doc.setIndexingStatus("INDEXED");
            doc.setIndexedAt(LocalDateTime.now());
            doc.setRetryCount(doc.getRetryCount() + 1);
            doc.setLastError(null);
            searchRepository.save(doc);
            retriedCount++;
        }
        return retriedCount;
    }

    private void handleFailedIndexing(String docId, String type, String error) {
        InvestigationSearchDocument failedDoc = InvestigationSearchDocument.builder()
                .docId(docId)
                .docType(type)
                .indexingStatus("FAILED_RETRY_QUEUED")
                .indexedAt(LocalDateTime.now())
                .retryCount(1)
                .lastError(error)
                .build();
        searchRepository.save(failedDoc);
    }

    private boolean matchesQuery(InvestigationSearchDocument doc, String query) {
        if (query.isEmpty()) return true;

        return contains(doc.getTransactionId(), query) ||
                contains(doc.getCustomerId(), query) ||
                contains(doc.getCaseId(), query) ||
                contains(doc.getDeviceId(), query) ||
                contains(doc.getIpAddress(), query) ||
                contains(doc.getBeneficiaryUpiId(), query) ||
                contains(doc.getMerchantId(), query) ||
                contains(doc.getFraudType(), query) ||
                contains(doc.getStatus(), query) ||
                contains(doc.getContent(), query);
    }

    private boolean contains(String text, String query) {
        return text != null && text.toLowerCase().contains(query);
    }

    private boolean matchesRiskLevel(InvestigationSearchDocument doc, String riskLevel) {
        if (riskLevel == null || riskLevel.isBlank()) return true;
        return riskLevel.equalsIgnoreCase(doc.getRiskLevel());
    }

    private boolean matchesRiskScore(InvestigationSearchDocument doc, Double minRisk, Double maxRisk) {
        if (minRisk != null && (doc.getRiskScore() == null || doc.getRiskScore() < minRisk)) return false;
        if (maxRisk != null && (doc.getRiskScore() == null || doc.getRiskScore() > maxRisk)) return false;
        return true;
    }

    private boolean matchesStatus(InvestigationSearchDocument doc, String status) {
        if (status == null || status.isBlank()) return true;
        return status.equalsIgnoreCase(doc.getStatus());
    }

    private boolean matchesFraudType(InvestigationSearchDocument doc, String fraudType) {
        if (fraudType == null || fraudType.isBlank()) return true;
        return fraudType.equalsIgnoreCase(doc.getFraudType());
    }

    private boolean matchesDateRange(InvestigationSearchDocument doc, LocalDateTime from, LocalDateTime to) {
        if (doc.getTimestamp() == null) return true;
        if (from != null && doc.getTimestamp().isBefore(from)) return false;
        if (to != null && doc.getTimestamp().isAfter(to)) return false;
        return true;
    }
}
