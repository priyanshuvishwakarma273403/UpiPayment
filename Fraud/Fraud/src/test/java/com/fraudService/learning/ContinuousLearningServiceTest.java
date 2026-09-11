package com.fraudService.learning;

import com.fraudService.learning.model.InvestigationLabel;
import com.fraudService.learning.model.ModelCandidate;
import com.fraudService.learning.service.ContinuousLearningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContinuousLearningServiceTest {

    private ContinuousLearningService learningService;

    @BeforeEach
    void setUp() {
        learningService = new ContinuousLearningService();
    }

    @Test
    void testLabelIngestionConfirmedFraudAndFalsePositive() {
        InvestigationLabel fraudLabel = InvestigationLabel.builder()
                .caseId("CASE_9901")
                .transactionId("TX9901")
                .customerId("C9901")
                .status("CONFIRMED_FRAUD")
                .analystId("ANALYST_01")
                .featureVector(Map.of("amount_deviation", 4.5, "velocity_1m", 8.0))
                .build();

        InvestigationLabel fpLabel = InvestigationLabel.builder()
                .caseId("CASE_9902")
                .transactionId("TX9902")
                .customerId("C9902")
                .status("FALSE_POSITIVE")
                .analystId("ANALYST_01")
                .featureVector(Map.of("amount_deviation", 0.5, "velocity_1m", 1.0))
                .build();

        InvestigationLabel ing1 = learningService.ingestLabel(fraudLabel);
        InvestigationLabel ing2 = learningService.ingestLabel(fpLabel);

        assertNotNull(ing1.getLabelId());
        assertEquals("CONFIRMED_FRAUD", ing1.getStatus());
        assertEquals(1.0, ing1.getNumericLabel());

        assertNotNull(ing2.getLabelId());
        assertEquals("FALSE_POSITIVE", ing2.getStatus());
        assertEquals(0.0, ing2.getNumericLabel());

        assertEquals(2, learningService.getIngestedLabels().size());
    }

    @Test
    void testContinuousRetrainingProducesCandidateInPendingApprovalStatus() {
        learningService.ingestLabel(InvestigationLabel.builder()
                .caseId("CASE_101")
                .status("CONFIRMED_FRAUD")
                .featureVector(Map.of("velocity_1m", 12.0))
                .build());

        ModelCandidate candidate = learningService.triggerRetrainingPipeline();

        assertNotNull(candidate);
        assertNotNull(candidate.getModelVersion());
        assertEquals("PENDING_APPROVAL", candidate.getStatus());
        assertNull(candidate.getApprovedBy());

        // Confirm active model has NOT changed automatically
        assertEquals("sentinelx-ml-v1.0.0", learningService.getActiveModelVersion());

        List<ModelCandidate> pending = learningService.getPendingCandidates();
        assertFalse(pending.isEmpty());
    }

    @Test
    void testHumanApprovalGatePromotesModelToDeployed() {
        learningService.ingestLabel(InvestigationLabel.builder()
                .caseId("CASE_102")
                .status("FALSE_POSITIVE")
                .featureVector(Map.of("amount_deviation", 0.2))
                .build());

        ModelCandidate candidate = learningService.triggerRetrainingPipeline();
        String candidateVersion = candidate.getModelVersion();

        ModelCandidate approved = learningService.approveAndDeployModel(candidateVersion, "LEAD_ANALYST_SARAH");

        assertNotNull(approved);
        assertEquals("DEPLOYED", approved.getStatus());
        assertEquals("LEAD_ANALYST_SARAH", approved.getApprovedBy());
        assertNotNull(approved.getApprovedAt());

        // Confirm active model version updated to approved candidate
        assertEquals(candidateVersion, learningService.getActiveModelVersion());
    }
}
