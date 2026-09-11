package com.upimesh.risk.controller;

import com.upimesh.risk.config.RiskThresholdConfig;
import com.upimesh.risk.exception.ProfileNotFoundException;
import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.entity.RiskScoringResult;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.model.response.ApiResponse;
import com.upimesh.risk.model.response.RiskScoringResponse;
import com.upimesh.risk.repository.RiskScoringResultRepository;
import com.upimesh.risk.rule.RiskRule;
import com.upimesh.risk.service.RiskScoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/risk")
@RequiredArgsConstructor
public class RiskScoringController {

    private final RiskScoringService riskScoringService;
    private final RiskScoringResultRepository scoringResultRepository;
    private final List<RiskRule> riskRules;
    private final RiskThresholdConfig thresholdConfig;

    @PostMapping("/score")
    public ResponseEntity<ApiResponse<RiskScoringResponse>> scoreTransaction(
            @Valid @RequestBody RiskScoringRequest request) {
        RiskScoringResponse response = riskScoringService.scoreTransaction(request);
        return ResponseEntity.ok(ApiResponse.success("Transaction risk score evaluated successfully", response));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<RiskProfile>> getUserRiskProfile(
            @PathVariable String userId) {
        RiskProfile profile = riskScoringService.getUserRiskProfile(userId);
        if (profile == null) {
            throw new ProfileNotFoundException("Risk profile not found for user ID: " + userId);
        }
        return ResponseEntity.ok(ApiResponse.success("User risk profile retrieved successfully", profile));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<List<RiskScoringResult>>> getRiskScoringHistory(
            @PathVariable String userId) {
        List<RiskScoringResult> history = scoringResultRepository.findByUserIdOrderByScoredAtDesc(userId);
        return ResponseEntity.ok(ApiResponse.success("Risk scoring history retrieved successfully", history));
    }

    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActiveRulesAndThresholds() {
        List<Map<String, String>> ruleList = riskRules.stream()
                .map(r -> Map.of("ruleId", r.getRuleId(), "ruleName", r.getRuleName()))
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("version", "v1.0.0");
        data.put("activeRuleCount", ruleList.size());
        data.put("rules", ruleList);
        data.put("thresholds", thresholdConfig);

        return ResponseEntity.ok(ApiResponse.success("Active risk rules and thresholds retrieved", data));
    }
}
