package com.fraudService.search;

import com.fraudService.entity.FraudCase;
import com.fraudService.entity.FraudLog;
import com.fraudService.repository.FraudCaseRepository;
import com.fraudService.repository.FraudLogRepository;
import com.fraudService.search.model.InvestigationSearchDocument;
import com.fraudService.search.model.SearchRequest;
import com.fraudService.search.model.SearchResult;
import com.fraudService.search.repository.InvestigationSearchRepository;
import com.fraudService.search.service.InvestigationSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestigationSearchServiceTest {

    @Mock
    private InvestigationSearchRepository searchRepository;

    @Mock
    private FraudLogRepository fraudLogRepository;

    @Mock
    private FraudCaseRepository fraudCaseRepository;

    private InvestigationSearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new InvestigationSearchService(searchRepository, fraudLogRepository, fraudCaseRepository);
    }

    @Test
    void testSearchByFreeTextQuery() {
        InvestigationSearchDocument doc1 = InvestigationSearchDocument.builder()
                .docId("DOC_TXN_PAY1001")
                .docType("TRANSACTION")
                .transactionId("PAY1001")
                .customerId("CUST_101")
                .deviceId("DEV_KNOWN_1")
                .ipAddress("127.0.0.1")
                .beneficiaryUpiId("alice@upimesh")
                .status("SAFE")
                .riskScore(0.05)
                .riskLevel("LOW")
                .timestamp(LocalDateTime.now())
                .content("Standard clean payment")
                .build();

        InvestigationSearchDocument doc2 = InvestigationSearchDocument.builder()
                .docId("DOC_CASE_CASE_99")
                .docType("CASE")
                .caseId("CASE_99")
                .customerId("CUST_99")
                .transactionId("TXN_BURST_505")
                .fraudType("ACCOUNT_TAKEOVER")
                .status("CONFIRMED_FRAUD")
                .riskScore(0.95)
                .riskLevel("CRITICAL")
                .timestamp(LocalDateTime.now())
                .content("SIM swap detected")
                .build();

        when(searchRepository.findAll()).thenReturn(List.of(doc1, doc2));

        SearchRequest req = SearchRequest.builder()
                .query("CASE_99")
                .build();

        SearchResult result = searchService.search(req);

        assertNotNull(result);
        assertEquals(1, result.getTotalHits());
        assertEquals("CASE_99", result.getDocuments().get(0).getCaseId());
    }

    @Test
    void testSearchWithRiskAndStatusFilters() {
        InvestigationSearchDocument doc1 = InvestigationSearchDocument.builder()
                .docId("DOC_TXN_PAY1002")
                .docType("TRANSACTION")
                .transactionId("PAY1002")
                .status("REVIEW")
                .riskScore(0.75)
                .riskLevel("HIGH")
                .timestamp(LocalDateTime.now())
                .build();

        InvestigationSearchDocument doc2 = InvestigationSearchDocument.builder()
                .docId("DOC_TXN_PAY1003")
                .docType("TRANSACTION")
                .transactionId("PAY1003")
                .status("SAFE")
                .riskScore(0.10)
                .riskLevel("LOW")
                .timestamp(LocalDateTime.now())
                .build();

        when(searchRepository.findAll()).thenReturn(List.of(doc1, doc2));

        SearchRequest req = SearchRequest.builder()
                .riskLevel("HIGH")
                .minRiskScore(0.50)
                .status("REVIEW")
                .build();

        SearchResult result = searchService.search(req);

        assertNotNull(result);
        assertEquals(1, result.getTotalHits());
        assertEquals("PAY1002", result.getDocuments().get(0).getTransactionId());
    }

    @Test
    void testIndexTransactionAndCase() {
        FraudLog logDoc = FraudLog.builder()
                .paymentId("PAY_NEW_1")
                .senderId(505L)
                .deviceId("DEV_505")
                .ipAddress("10.0.0.1")
                .finalDecision("BLOCKED")
                .riskScore(0.90)
                .riskLevel("CRITICAL")
                .checkedAt(LocalDateTime.now())
                .reasons("Money mule burst")
                .build();

        when(searchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        searchService.indexTransaction(logDoc);

        verify(searchRepository, times(1)).save(any(InvestigationSearchDocument.class));
    }

    @Test
    void testFailedIndexingResilienceAndRetry() {
        InvestigationSearchDocument failedDoc = InvestigationSearchDocument.builder()
                .docId("DOC_FAILED_1")
                .indexingStatus("FAILED_RETRY_QUEUED")
                .retryCount(1)
                .build();

        when(searchRepository.findByIndexingStatus("FAILED_RETRY_QUEUED")).thenReturn(List.of(failedDoc));

        int retried = searchService.retryFailedIndexing();

        assertEquals(1, retried);
        assertEquals("INDEXED", failedDoc.getIndexingStatus());
        assertEquals(2, failedDoc.getRetryCount());
        verify(searchRepository, times(1)).save(failedDoc);
    }
}
