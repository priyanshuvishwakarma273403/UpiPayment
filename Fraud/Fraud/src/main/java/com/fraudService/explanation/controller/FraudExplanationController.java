package com.fraudService.explanation.controller;

import com.fraudService.dto.request.FraudCheckRequest;
import com.fraudService.explanation.model.ExplainableFraudDecision;
import com.fraudService.explanation.service.FraudExplanationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fraud/explain")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Explainable Fraud Decisions", description = "Transparent, evidence-based decision explainability APIs")
public class FraudExplanationController {

    private final FraudExplanationService explanationService;

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get empirical explainable fraud decision report for a payment ID")
    public ResponseEntity<ExplainableFraudDecision> explainPaymentId(@PathVariable String paymentId) {
        log.info("REST: Request explainability report for paymentId={}", paymentId);
        return ResponseEntity.ok(explanationService.explainPaymentId(paymentId));
    }

    @PostMapping
    @Operation(summary = "Generate empirical explainable fraud decision report for a fraud check request")
    public ResponseEntity<ExplainableFraudDecision> explainTransaction(@Valid @RequestBody FraudCheckRequest request) {
        log.info("REST: Generate explainability report for paymentId={}", request.getPaymentId());
        return ResponseEntity.ok(explanationService.explainTransaction(request));
    }
}
