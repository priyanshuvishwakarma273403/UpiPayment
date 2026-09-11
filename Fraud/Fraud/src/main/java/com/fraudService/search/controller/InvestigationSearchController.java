package com.fraudService.search.controller;

import com.fraudService.search.model.InvestigationSearchDocument;
import com.fraudService.search.model.SearchRequest;
import com.fraudService.search.model.SearchResult;
import com.fraudService.search.service.InvestigationSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fraud/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Investigation Search", description = "OpenSearch-backed unified multi-entity investigation search APIs")
public class InvestigationSearchController {

    private final InvestigationSearchService searchService;

    @GetMapping
    @Operation(summary = "Unified multi-entity investigation search across transactions, customers, cases, devices, IPs, beneficiaries, and merchants")
    public ResponseEntity<SearchResult> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) Double minRiskScore,
            @RequestParam(required = false) Double maxRiskScore,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fraudType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SearchRequest req = SearchRequest.builder()
                .query(query)
                .riskLevel(riskLevel)
                .minRiskScore(minRiskScore)
                .maxRiskScore(maxRiskScore)
                .status(status)
                .fraudType(fraudType)
                .fromDate(fromDate)
                .toDate(toDate)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(searchService.search(req));
    }

    @PostMapping("/reindex")
    @Operation(summary = "Trigger complete reindexing of transaction and fraud case documents into OpenSearch store")
    public ResponseEntity<Map<String, Object>> reindexAll() {
        int count = searchService.reindexAll();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "indexedDocumentsCount", count,
                "reindexedAt", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/failed-indexing")
    @Operation(summary = "Get eventual consistency failed indexing queue")
    public ResponseEntity<List<InvestigationSearchDocument>> getFailedIndexingQueue() {
        return ResponseEntity.ok(searchService.getFailedIndexingQueue());
    }

    @PostMapping("/retry-failed")
    @Operation(summary = "Retry failed indexing documents to enforce eventual consistency")
    public ResponseEntity<Map<String, Object>> retryFailedIndexing() {
        int retried = searchService.retryFailedIndexing();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "retriedCount", retried,
                "retriedAt", LocalDateTime.now().toString()
        ));
    }
}
