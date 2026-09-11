package com.fraudService.learning.controller;

import com.fraudService.learning.model.InvestigationLabel;
import com.fraudService.learning.model.ModelCandidate;
import com.fraudService.learning.service.ContinuousLearningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Phase 20 Continuous Fraud Model Learning.
 * Exposes investigator outcome label ingestion, training pipeline triggering,
 * candidate model inspection, human approval deployment gate, and history.
 */
@RestController
@RequestMapping("/fraud/learning")
@RequiredArgsConstructor
@Slf4j
public class ContinuousLearningController {

    private final ContinuousLearningService learningService;

    @PostMapping("/labels")
    public ResponseEntity<InvestigationLabel> ingestLabel(@RequestBody InvestigationLabel label) {
        log.info("Received investigator resolution label for caseId={}", label.getCaseId());
        InvestigationLabel result = learningService.ingestLabel(label);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/labels")
    public ResponseEntity<List<InvestigationLabel>> getLabels() {
        return ResponseEntity.ok(learningService.getIngestedLabels());
    }

    @PostMapping("/train")
    public ResponseEntity<ModelCandidate> triggerRetraining() {
        log.info("Triggering continuous learning retraining pipeline...");
        ModelCandidate candidate = learningService.triggerRetrainingPipeline();
        return ResponseEntity.ok(candidate);
    }

    @GetMapping("/candidates")
    public ResponseEntity<List<ModelCandidate>> getCandidates() {
        return ResponseEntity.ok(learningService.getPendingCandidates());
    }

    @PostMapping("/approve/{modelVersion}")
    public ResponseEntity<ModelCandidate> approveModel(@PathVariable String modelVersion,
                                                       @RequestParam(required = false, defaultValue = "HUMAN_ANALYST") String approvedBy) {
        log.info("Approving candidate model {} by analyst {}", modelVersion, approvedBy);
        ModelCandidate approved = learningService.approveAndDeployModel(modelVersion, approvedBy);
        return ResponseEntity.ok(approved);
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory() {
        return ResponseEntity.ok(Map.of(
                "activeModelVersion", learningService.getActiveModelVersion(),
                "history", learningService.getModelHistory()
        ));
    }
}
