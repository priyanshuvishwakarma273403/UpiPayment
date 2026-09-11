package com.fraudService.service;

import com.fraudService.dto.request.AssignCaseRequest;
import com.fraudService.dto.request.CaseActionRequest;
import com.fraudService.dto.request.CreateCaseRequest;
import com.fraudService.entity.FraudCase;
import com.fraudService.entity.FraudCaseAudit;
import com.fraudService.repository.FraudCaseAuditRepository;
import com.fraudService.repository.FraudCaseRepository;
import com.fraudService.security.FraudCaseSecurityEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudCaseServiceTest {

    @Mock
    private FraudCaseRepository caseRepository;

    @Mock
    private FraudCaseAuditRepository auditRepository;

    private FraudCaseSecurityEvaluator securityEvaluator;
    private FraudCaseService caseService;

    private final String ANALYST_ID = "ANALYST_101";
    private final String ANALYST_ROLE = "ROLE_ANALYST";

    @BeforeEach
    void setUp() {
        securityEvaluator = new FraudCaseSecurityEvaluator();
        caseService = new FraudCaseService(caseRepository, auditRepository, securityEvaluator);
    }

    @Test
    void testCreateCaseWithValidAuthorization() {
        CreateCaseRequest req = CreateCaseRequest.builder()
                .customerId("CUST_8899")
                .transactionId("TXN_9900")
                .fraudType("ACCOUNT_TAKEOVER")
                .severity("HIGH")
                .initialComment("Suspected device takeover burst")
                .build();

        when(caseRepository.save(any(FraudCase.class))).thenAnswer(inv -> inv.getArgument(0));

        FraudCase created = caseService.createCase(req, ANALYST_ID, ANALYST_ROLE);

        assertNotNull(created);
        assertNotNull(created.getCaseId());
        assertTrue(created.getCaseId().startsWith("CASE_"));
        assertEquals("CUST_8899", created.getCustomerId());
        assertEquals("TXN_9900", created.getTransactionId());
        assertEquals("ACCOUNT_TAKEOVER", created.getFraudType());
        assertEquals("HIGH", created.getSeverity());
        assertEquals("OPEN", created.getStatus());

        ArgumentCaptor<FraudCaseAudit> auditCaptor = ArgumentCaptor.forClass(FraudCaseAudit.class);
        verify(auditRepository, times(1)).save(auditCaptor.capture());
        FraudCaseAudit audit = auditCaptor.getValue();
        assertEquals(created.getCaseId(), audit.getCaseId());
        assertEquals(ANALYST_ID, audit.getActor());
        assertEquals("CREATE", audit.getAction());
        assertNull(audit.getOldState());
        assertEquals("OPEN", audit.getNewState());
    }

    @Test
    void testCreateCaseUnauthorizedThrowsSecurityException() {
        CreateCaseRequest req = CreateCaseRequest.builder()
                .customerId("CUST_8899")
                .transactionId("TXN_9900")
                .fraudType("ACCOUNT_TAKEOVER")
                .build();

        assertThrows(SecurityException.class, () ->
                caseService.createCase(req, "USER_101", "ROLE_USER"));

        assertThrows(SecurityException.class, () ->
                caseService.createCase(req, null, "ROLE_ANALYST"));
    }

    @Test
    void testAssignCaseTransitionsOpenToInvestigating() {
        FraudCase existing = FraudCase.builder()
                .caseId("CASE_1001")
                .customerId("CUST_8899")
                .transactionId("TXN_9900")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        when(caseRepository.findByCaseId("CASE_1001")).thenReturn(Optional.of(existing));
        when(caseRepository.save(any(FraudCase.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignCaseRequest assignReq = AssignCaseRequest.builder()
                .assignedTo("INVESTIGATOR_404")
                .comment("Assigning for detailed log inspection")
                .build();

        FraudCase updated = caseService.assignCase("CASE_1001", assignReq, ANALYST_ID, ANALYST_ROLE);

        assertNotNull(updated);
        assertEquals("INVESTIGATOR_404", updated.getAssignedTo());
        assertEquals("INVESTIGATING", updated.getStatus());

        ArgumentCaptor<FraudCaseAudit> auditCaptor = ArgumentCaptor.forClass(FraudCaseAudit.class);
        verify(auditRepository, times(1)).save(auditCaptor.capture());
        FraudCaseAudit audit = auditCaptor.getValue();
        assertEquals("ASSIGN", audit.getAction());
        assertEquals("OPEN", audit.getOldState());
        assertEquals("INVESTIGATING", audit.getNewState());
    }

    @Test
    void testAddCommentAppendsAuditTrailWithoutStateChange() {
        FraudCase existing = FraudCase.builder()
                .caseId("CASE_1002")
                .customerId("CUST_8899")
                .status("INVESTIGATING")
                .assignedTo("INVESTIGATOR_404")
                .createdAt(LocalDateTime.now())
                .build();

        when(caseRepository.findByCaseId("CASE_1002")).thenReturn(Optional.of(existing));
        when(caseRepository.save(any(FraudCase.class))).thenAnswer(inv -> inv.getArgument(0));

        FraudCase updated = caseService.addComment("CASE_1002", "Verified customer IP is consistent with VPN proxy", ANALYST_ID, ANALYST_ROLE);

        assertEquals("INVESTIGATING", updated.getStatus());

        ArgumentCaptor<FraudCaseAudit> auditCaptor = ArgumentCaptor.forClass(FraudCaseAudit.class);
        verify(auditRepository, times(1)).save(auditCaptor.capture());
        FraudCaseAudit audit = auditCaptor.getValue();
        assertEquals("COMMENT", audit.getAction());
        assertEquals("INVESTIGATING", audit.getOldState());
        assertEquals("INVESTIGATING", audit.getNewState());
        assertEquals("Verified customer IP is consistent with VPN proxy", audit.getDetails());
    }

    @Test
    void testEscalateCaseTransitionsToEscalated() {
        FraudCase existing = FraudCase.builder()
                .caseId("CASE_1003")
                .status("INVESTIGATING")
                .assignedTo("INVESTIGATOR_404")
                .build();

        when(caseRepository.findByCaseId("CASE_1003")).thenReturn(Optional.of(existing));
        when(caseRepository.save(any(FraudCase.class))).thenAnswer(inv -> inv.getArgument(0));

        CaseActionRequest actionReq = CaseActionRequest.builder()
                .reason("Complex cross-account money mule network detected")
                .build();

        FraudCase updated = caseService.escalateCase("CASE_1003", actionReq, ANALYST_ID, ANALYST_ROLE);

        assertEquals("ESCALATED", updated.getStatus());

        ArgumentCaptor<FraudCaseAudit> auditCaptor = ArgumentCaptor.forClass(FraudCaseAudit.class);
        verify(auditRepository, times(1)).save(auditCaptor.capture());
        FraudCaseAudit audit = auditCaptor.getValue();
        assertEquals("ESCALATE", audit.getAction());
        assertEquals("INVESTIGATING", audit.getOldState());
        assertEquals("ESCALATED", audit.getNewState());
    }

    @Test
    void testMarkFraudTransitionsToConfirmedFraud() {
        FraudCase existing = FraudCase.builder()
                .caseId("CASE_1004")
                .status("ESCALATED")
                .assignedTo("INVESTIGATOR_404")
                .build();

        when(caseRepository.findByCaseId("CASE_1004")).thenReturn(Optional.of(existing));
        when(caseRepository.save(any(FraudCase.class))).thenAnswer(inv -> inv.getArgument(0));

        CaseActionRequest actionReq = CaseActionRequest.builder()
                .reason("Confirmed unauthorized SIM swap and unauthorized UPI registration")
                .build();

        FraudCase updated = caseService.markFraud("CASE_1004", actionReq, ANALYST_ID, ANALYST_ROLE);

        assertEquals("CONFIRMED_FRAUD", updated.getStatus());
        assertEquals("CONFIRMED_FRAUD", updated.getResolution());
        assertEquals("Confirmed unauthorized SIM swap and unauthorized UPI registration", updated.getResolutionReason());

        ArgumentCaptor<FraudCaseAudit> auditCaptor = ArgumentCaptor.forClass(FraudCaseAudit.class);
        verify(auditRepository, times(1)).save(auditCaptor.capture());
        FraudCaseAudit audit = auditCaptor.getValue();
        assertEquals("MARK_FRAUD", audit.getAction());
        assertEquals("ESCALATED", audit.getOldState());
        assertEquals("CONFIRMED_FRAUD", audit.getNewState());
    }

    @Test
    void testMarkFalsePositiveTransitionsToFalsePositive() {
        FraudCase existing = FraudCase.builder()
                .caseId("CASE_1005")
                .status("INVESTIGATING")
                .assignedTo("INVESTIGATOR_404")
                .build();

        when(caseRepository.findByCaseId("CASE_1005")).thenReturn(Optional.of(existing));
        when(caseRepository.save(any(FraudCase.class))).thenAnswer(inv -> inv.getArgument(0));

        CaseActionRequest actionReq = CaseActionRequest.builder()
                .reason("Customer verified legitimate travel to new location via phone OTP")
                .build();

        FraudCase updated = caseService.markFalsePositive("CASE_1005", actionReq, ANALYST_ID, ANALYST_ROLE);

        assertEquals("FALSE_POSITIVE", updated.getStatus());
        assertEquals("FALSE_POSITIVE", updated.getResolution());

        ArgumentCaptor<FraudCaseAudit> auditCaptor = ArgumentCaptor.forClass(FraudCaseAudit.class);
        verify(auditRepository, times(1)).save(auditCaptor.capture());
        FraudCaseAudit audit = auditCaptor.getValue();
        assertEquals("MARK_FALSE_POSITIVE", audit.getAction());
        assertEquals("INVESTIGATING", audit.getOldState());
        assertEquals("FALSE_POSITIVE", audit.getNewState());
    }

    @Test
    void testResolveCaseTransitionsToResolved() {
        FraudCase existing = FraudCase.builder()
                .caseId("CASE_1006")
                .status("CONFIRMED_FRAUD")
                .resolution("CONFIRMED_FRAUD")
                .build();

        when(caseRepository.findByCaseId("CASE_1006")).thenReturn(Optional.of(existing));
        when(caseRepository.save(any(FraudCase.class))).thenAnswer(inv -> inv.getArgument(0));

        CaseActionRequest actionReq = CaseActionRequest.builder()
                .resolution("CONFIRMED_FRAUD_ACCOUNT_BLOCKED")
                .reason("Account permanently suspended and chargeback initiated")
                .build();

        FraudCase updated = caseService.resolveCase("CASE_1006", actionReq, ANALYST_ID, ANALYST_ROLE);

        assertEquals("RESOLVED", updated.getStatus());
        assertEquals("CONFIRMED_FRAUD_ACCOUNT_BLOCKED", updated.getResolution());

        ArgumentCaptor<FraudCaseAudit> auditCaptor = ArgumentCaptor.forClass(FraudCaseAudit.class);
        verify(auditRepository, times(1)).save(auditCaptor.capture());
        FraudCaseAudit audit = auditCaptor.getValue();
        assertEquals("RESOLVE", audit.getAction());
        assertEquals("CONFIRMED_FRAUD", audit.getOldState());
        assertEquals("RESOLVED", audit.getNewState());
    }

    @Test
    void testInvalidStateTransitionOnResolvedCaseThrowsIllegalStateException() {
        FraudCase resolved = FraudCase.builder()
                .caseId("CASE_1007")
                .status("RESOLVED")
                .resolution("RESOLVED_NO_ACTION")
                .build();

        when(caseRepository.findByCaseId("CASE_1007")).thenReturn(Optional.of(resolved));

        CaseActionRequest actionReq = CaseActionRequest.builder().reason("Try escalate").build();

        assertThrows(IllegalStateException.class, () ->
                caseService.escalateCase("CASE_1007", actionReq, ANALYST_ID, ANALYST_ROLE));

        assertThrows(IllegalStateException.class, () ->
                caseService.markFraud("CASE_1007", actionReq, ANALYST_ID, ANALYST_ROLE));

        assertThrows(IllegalStateException.class, () ->
                caseService.markFalsePositive("CASE_1007", actionReq, ANALYST_ID, ANALYST_ROLE));
    }
}
