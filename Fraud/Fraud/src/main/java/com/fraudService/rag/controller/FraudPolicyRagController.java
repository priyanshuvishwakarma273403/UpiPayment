package com.fraudService.rag.controller;

import com.fraudService.rag.model.GroundedRagResponse;
import com.fraudService.rag.model.PolicyQueryResult;
import com.fraudService.rag.service.FraudPolicyRagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fraud/policy")
@RequiredArgsConstructor
@Slf4j
public class FraudPolicyRagController {

    private final FraudPolicyRagService ragService;

    @PostMapping("/search")
    public ResponseEntity<List<PolicyQueryResult>> searchPolicy(@RequestBody Map<String, Object> payload) {
        String query = (String) payload.getOrDefault("query", "");
        int topK = (int) payload.getOrDefault("topK", 3);
        log.info("REST POST /fraud/policy/search | query='{}'", query);

        List<PolicyQueryResult> results = ragService.searchPolicy(query, topK);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/query")
    public ResponseEntity<GroundedRagResponse> queryGroundedRag(@RequestBody Map<String, Object> payload) {
        String query = (String) payload.getOrDefault("query", "");
        @SuppressWarnings("unchecked")
        List<String> observedData = (List<String>) payload.get("observedData");
        @SuppressWarnings("unchecked")
        List<String> modelPrediction = (List<String>) payload.get("modelPrediction");

        log.info("REST POST /fraud/policy/query | query='{}'", query);
        GroundedRagResponse response = ragService.queryGroundedRag(query, observedData, modelPrediction);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, String>> ingestPolicy(@RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String markdown = payload.get("markdown");
        log.info("REST POST /fraud/policy/ingest | title='{}'", title);

        ragService.ingestDocument(title, markdown);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Policy document ingested successfully"));
    }
}
