package com.fraudService.service;

import com.fraudService.dto.request.AssignCaseRequest;
import com.fraudService.dto.request.CaseActionRequest;
import com.fraudService.dto.request.CreateCaseRequest;
import com.fraudService.entity.FraudCase;
import com.fraudService.entity.FraudCaseAudit;
import com.fraudService.repository.FraudCaseAuditRepository;
import com.fraudService.repository.FraudCaseRepository;
import com.fraudService.security.FraudCaseSecurityEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudCaseService {

    private final FraudCaseRepository caseRepository;
    private final FraudCaseAuditRepository auditRepository;
    private final FraudCaseSecurityEvaluator securityEvaluator;

    public FraudCase createCase(CreateCaseRequest request, String actor, String rolesHeader) {
        securityEvaluator.validateAuthorization(actor, rolesHeader);

        String initialStatus = (request.getAssignedTo() != null && !request.getAssignedTo().trim().isEmpty())
                ? "INVESTIGATING" : "OPEN";

        String caseId = "CASE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        FraudCase fraudCase = FraudCase.builder()
                .caseId(caseId)
                .customerId(request.getCustomerId())
                .transactionId(request.getTransactionId())
                .fraudType(request.getFraudType())
                .severity(request.getSeverity() != null ? request.getSeverity().toUpperCase() : "MEDIUM")
                .status(initialStatus)
                .assignedTo(request.getAssignedTo())
                .createdAt(now)
                .updatedAt(now)
                .build();

        FraudCase saved = caseRepository.save(fraudCase);

        recordAudit(caseId, actor, "CREATE", null, initialStatus,
                "Case created with initial status " + initialStatus +
                        (request.getInitialComment() != null ? ". Note: " + request.getInitialComment() : ""));

        log.info("Created fraud caseId={} for customerId={} by actor={}", caseId, request.getCustomerId(), actor);
        return saved;
    }

    public FraudCase assignCase(String caseId, AssignCaseRequest request, String actor, String rolesHeader) {
        securityEvaluator.validateAuthorization(actor, rolesHeader);

        FraudCase fraudCase = getCaseOrThrow(caseId);
        String oldState = fraudCase.getStatus();

        fraudCase.setAssignedTo(request.getAssignedTo());

        // Auto transition OPEN to INVESTIGATING upon assignment
        if ("OPEN".equalsIgnoreCase(oldState)) {
            fraudCase.setStatus("INVESTIGATING");
        }
        fraudCase.setUpdatedAt(LocalDateTime.now());

        FraudCase updated = caseRepository.save(fraudCase);

        String details = "Assigned case to " + request.getAssignedTo() +
                (request.getComment() != null ? ". Comment: " + request.getComment() : "");
        recordAudit(caseId, actor, "ASSIGN", oldState, updated.getStatus(), details);

        log.info("Assigned fraud caseId={} to {} by actor={}", caseId, request.getAssignedTo(), actor);
        return updated;
    }

    public FraudCase addComment(String caseId, String comment, String actor, String rolesHeader) {
        securityEvaluator.validateAuthorization(actor, rolesHeader);

        FraudCase fraudCase = getCaseOrThrow(caseId);
        fraudCase.setUpdatedAt(LocalDateTime.now());
        FraudCase updated = caseRepository.save(fraudCase);

        recordAudit(caseId, actor, "COMMENT", fraudCase.getStatus(), fraudCase.getStatus(), comment);

        log.info("Added comment to fraud caseId={} by actor={}", caseId, actor);
        return updated;
    }

    public FraudCase escalateCase(String caseId, CaseActionRequest request, String actor, String rolesHeader) {
        securityEvaluator.validateAuthorization(actor, rolesHeader);

        FraudCase fraudCase = getCaseOrThrow(caseId);
        String oldState = fraudCase.getStatus();

        if ("RESOLVED".equalsIgnoreCase(oldState)) {
            throw new IllegalStateException("Cannot escalate a case that is already RESOLVED.");
        }

        fraudCase.setStatus("ESCALATED");
        fraudCase.setUpdatedAt(LocalDateTime.now());
        FraudCase updated = caseRepository.save(fraudCase);

        String reason = request != null && request.getReason() != null ? request.getReason() : "Escalated for senior review";
        recordAudit(caseId, actor, "ESCALATE", oldState, "ESCALATED", reason);

        log.info("Escalated fraud caseId={} by actor={}", caseId, actor);
        return updated;
    }

    public FraudCase markFraud(String caseId, CaseActionRequest request, String actor, String rolesHeader) {
        securityEvaluator.validateAuthorization(actor, rolesHeader);

        FraudCase fraudCase = getCaseOrThrow(caseId);
        String oldState = fraudCase.getStatus();

        if ("RESOLVED".equalsIgnoreCase(oldState)) {
            throw new IllegalStateException("Cannot mark fraud on a case that is already RESOLVED.");
        }

        String reason = request != null && request.getReason() != null ? request.getReason() : "Confirmed fraud activity";
        fraudCase.setStatus("CONFIRMED_FRAUD");
        fraudCase.setResolution("CONFIRMED_FRAUD");
        fraudCase.setResolutionReason(reason);
        fraudCase.setUpdatedAt(LocalDateTime.now());

        FraudCase updated = caseRepository.save(fraudCase);
        recordAudit(caseId, actor, "MARK_FRAUD", oldState, "CONFIRMED_FRAUD", reason);

        log.info("Marked fraud caseId={} as CONFIRMED_FRAUD by actor={}", caseId, actor);
        return updated;
    }

    public FraudCase markFalsePositive(String caseId, CaseActionRequest request, String actor, String rolesHeader) {
        securityEvaluator.validateAuthorization(actor, rolesHeader);

        FraudCase fraudCase = getCaseOrThrow(caseId);
        String oldState = fraudCase.getStatus();

        if ("RESOLVED".equalsIgnoreCase(oldState)) {
            throw new IllegalStateException("Cannot mark false positive on a case that is already RESOLVED.");
        }

        String reason = request != null && request.getReason() != null ? request.getReason() : "Verified legitimate transaction";
        fraudCase.setStatus("FALSE_POSITIVE");
        fraudCase.setResolution("FALSE_POSITIVE");
        fraudCase.setResolutionReason(reason);
        fraudCase.setUpdatedAt(LocalDateTime.now());

        FraudCase updated = caseRepository.save(fraudCase);
        recordAudit(caseId, actor, "MARK_FALSE_POSITIVE", oldState, "FALSE_POSITIVE", reason);

        log.info("Marked fraud caseId={} as FALSE_POSITIVE by actor={}", caseId, actor);
        return updated;
    }

    public FraudCase resolveCase(String caseId, CaseActionRequest request, String actor, String rolesHeader) {
        securityEvaluator.validateAuthorization(actor, rolesHeader);

        FraudCase fraudCase = getCaseOrThrow(caseId);
        String oldState = fraudCase.getStatus();

        String resolution = (request != null && request.getResolution() != null)
                ? request.getResolution()
                : (fraudCase.getResolution() != null ? fraudCase.getResolution() : "RESOLVED_NO_ACTION");

        String reason = (request != null && request.getReason() != null)
                ? request.getReason()
                : "Investigation complete";

        fraudCase.setStatus("RESOLVED");
        fraudCase.setResolution(resolution);
        fraudCase.setResolutionReason(reason);
        fraudCase.setUpdatedAt(LocalDateTime.now());

        FraudCase updated = caseRepository.save(fraudCase);
        recordAudit(caseId, actor, "RESOLVE", oldState, "RESOLVED", "Resolution: " + resolution + ". Reason: " + reason);

        log.info("Resolved fraud caseId={} by actor={}", caseId, actor);
        return updated;
    }

    public FraudCase getCaseById(String caseId) {
        return getCaseOrThrow(caseId);
    }

    public List<FraudCase> searchCases(String status, String assignedTo, String customerId, String transactionId) {
        if (status != null && assignedTo != null) {
            return caseRepository.findByStatusAndAssignedTo(status.toUpperCase(), assignedTo);
        }
        if (status != null) {
            return caseRepository.findByStatus(status.toUpperCase());
        }
        if (assignedTo != null) {
            return caseRepository.findByAssignedTo(assignedTo);
        }
        if (customerId != null) {
            return caseRepository.findByCustomerId(customerId);
        }
        if (transactionId != null) {
            return caseRepository.findByTransactionId(transactionId);
        }
        return caseRepository.findAll();
    }

    public List<FraudCaseAudit> getCaseAudits(String caseId) {
        return auditRepository.findByCaseIdOrderByTimestampDesc(caseId);
    }

    private FraudCase getCaseOrThrow(String caseId) {
        return caseRepository.findByCaseId(caseId)
                .orElseGet(() -> caseRepository.findById(caseId)
                        .orElseThrow(() -> new IllegalArgumentException("Fraud case not found with ID: " + caseId)));
    }

    private void recordAudit(String caseId, String actor, String action, String oldState, String newState, String details) {
        FraudCaseAudit audit = FraudCaseAudit.builder()
                .caseId(caseId)
                .actor(actor)
                .action(action)
                .timestamp(LocalDateTime.now())
                .entity("FraudCase")
                .oldState(oldState)
                .newState(newState)
                .details(details)
                .build();
        auditRepository.save(audit);
    }
}
