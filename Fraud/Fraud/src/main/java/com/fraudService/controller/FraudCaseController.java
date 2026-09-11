package com.fraudService.controller;

import com.fraudService.dto.request.AssignCaseRequest;
import com.fraudService.dto.request.CaseActionRequest;
import com.fraudService.dto.request.CreateCaseRequest;
import com.fraudService.entity.FraudCase;
import com.fraudService.entity.FraudCaseAudit;
import com.fraudService.service.FraudCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fraud/cases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fraud Case Management", description = "Real-time investigator case workflow APIs")
public class FraudCaseController {

    private final FraudCaseService caseService;

    @PostMapping
    @Operation(summary = "Create a new fraud investigation case")
    public ResponseEntity<FraudCase> createCase(
            @Valid @RequestBody CreateCaseRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actor,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {
        log.info("REST: Create case request by actor={}", actor);
        FraudCase created = caseService.createCase(request, actor, rolesHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{caseId}")
    @Operation(summary = "Get fraud case details by ID")
    public ResponseEntity<FraudCase> getCase(@PathVariable String caseId) {
        return ResponseEntity.ok(caseService.getCaseById(caseId));
    }

    @GetMapping
    @Operation(summary = "Search or query fraud cases")
    public ResponseEntity<List<FraudCase>> searchCases(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String transactionId) {
        return ResponseEntity.ok(caseService.searchCases(status, assignedTo, customerId, transactionId));
    }

    @PostMapping("/{caseId}/assign")
    @Operation(summary = "Assign a case to an analyst/investigator")
    public ResponseEntity<FraudCase> assignCase(
            @PathVariable String caseId,
            @Valid @RequestBody AssignCaseRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actor,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {
        return ResponseEntity.ok(caseService.assignCase(caseId, request, actor, rolesHeader));
    }

    @PostMapping("/{caseId}/comment")
    @Operation(summary = "Add an investigation comment to a case")
    public ResponseEntity<FraudCase> addComment(
            @PathVariable String caseId,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String actor,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {
        String comment = body != null ? body.get("comment") : null;
        return ResponseEntity.ok(caseService.addComment(caseId, comment, actor, rolesHeader));
    }

    @PostMapping("/{caseId}/escalate")
    @Operation(summary = "Escalate a case for senior review")
    public ResponseEntity<FraudCase> escalateCase(
            @PathVariable String caseId,
            @RequestBody(required = false) CaseActionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actor,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {
        return ResponseEntity.ok(caseService.escalateCase(caseId, request, actor, rolesHeader));
    }

    @PostMapping("/{caseId}/mark-fraud")
    @Operation(summary = "Mark case as CONFIRMED_FRAUD")
    public ResponseEntity<FraudCase> markFraud(
            @PathVariable String caseId,
            @RequestBody(required = false) CaseActionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actor,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {
        return ResponseEntity.ok(caseService.markFraud(caseId, request, actor, rolesHeader));
    }

    @PostMapping("/{caseId}/mark-false-positive")
    @Operation(summary = "Mark case as FALSE_POSITIVE")
    public ResponseEntity<FraudCase> markFalsePositive(
            @PathVariable String caseId,
            @RequestBody(required = false) CaseActionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actor,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {
        return ResponseEntity.ok(caseService.markFalsePositive(caseId, request, actor, rolesHeader));
    }

    @PostMapping("/{caseId}/resolve")
    @Operation(summary = "Resolve a case")
    public ResponseEntity<FraudCase> resolveCase(
            @PathVariable String caseId,
            @RequestBody(required = false) CaseActionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String actor,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader) {
        return ResponseEntity.ok(caseService.resolveCase(caseId, request, actor, rolesHeader));
    }

    @GetMapping("/{caseId}/audits")
    @Operation(summary = "Get audit history for a fraud case")
    public ResponseEntity<List<FraudCaseAudit>> getCaseAudits(@PathVariable String caseId) {
        return ResponseEntity.ok(caseService.getCaseAudits(caseId));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
