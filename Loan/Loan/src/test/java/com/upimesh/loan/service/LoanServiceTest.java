package com.upimesh.loan.service;

import com.upimesh.loan.feign.NpciServiceClient;
import com.upimesh.loan.feign.dto.InitiateTransactionRequest;
import com.upimesh.loan.feign.dto.NpciApiResponse;
import com.upimesh.loan.feign.dto.TransactionResponse;
import com.upimesh.loan.feign.dto.TransactionStatus;
import com.upimesh.loan.model.entity.LoanApplication;
import com.upimesh.loan.model.entity.LoanRepayment;
import com.upimesh.loan.model.enums.LoanStatus;
import com.upimesh.loan.model.enums.LoanType;
import com.upimesh.loan.model.enums.RepaymentStatus;
import com.upimesh.loan.repository.LoanApplicationRepository;
import com.upimesh.loan.repository.LoanRepaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanApplicationRepository applicationRepository;

    @Mock
    private LoanRepaymentRepository repaymentRepository;

    @Mock
    private CreditScoringService creditScoringService;

    @Spy
    private EmiCalculationService emiCalculationService;

    @Mock
    private NpciServiceClient npciServiceClient;

    @InjectMocks
    private LoanService loanService;

    private String userId;
    private String userUpiId;

    @BeforeEach
    public void setUp() {
        userId = "user123";
        userUpiId = "user123@upimesh";
    }

    // ─── 1. CREDIT SCORING DECISION ENGINE TESTS ─────────────────────────────

    @Test
    public void testCreditDecision_HighCreditScore() {
        CreditScoringService scoringService = new CreditScoringService();
        BigDecimal requested = BigDecimal.valueOf(10000.00);

        CreditScoringService.CreditDecision decision = scoringService.scoreToCreditDecision(800, requested);

        assertTrue(decision.isApproved());
        assertEquals(requested, decision.getApprovedAmount());
        assertEquals(BigDecimal.valueOf(12.00), decision.getInterestRate());
        assertNull(decision.getRejectionReason());
    }

    @Test
    public void testCreditDecision_MediumCreditScore() {
        CreditScoringService scoringService = new CreditScoringService();
        BigDecimal requested = BigDecimal.valueOf(10000.00);

        CreditScoringService.CreditDecision decision = scoringService.scoreToCreditDecision(700, requested);

        assertTrue(decision.isApproved());
        BigDecimal expectedApproved = requested.multiply(BigDecimal.valueOf(0.70)).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedApproved, decision.getApprovedAmount());
        assertEquals(BigDecimal.valueOf(18.00), decision.getInterestRate());
        assertNull(decision.getRejectionReason());
    }

    @Test
    public void testCreditDecision_LowCreditScore() {
        CreditScoringService scoringService = new CreditScoringService();
        BigDecimal requested = BigDecimal.valueOf(10000.00);

        CreditScoringService.CreditDecision decision = scoringService.scoreToCreditDecision(600, requested);

        assertFalse(decision.isApproved());
        assertEquals(BigDecimal.ZERO, decision.getApprovedAmount());
        assertEquals(BigDecimal.ZERO, decision.getInterestRate());
        assertNotNull(decision.getRejectionReason());
    }

    // ─── 2. EMI FORMULA COMPUTATION TESTS ────────────────────────────────────

    @Test
    public void testEmiCalculation_InterestFree() {
        EmiCalculationService calculationService = new EmiCalculationService();
        BigDecimal principal = BigDecimal.valueOf(1200.00);
        BigDecimal annualInterest = BigDecimal.ZERO;
        int tenure = 12;

        BigDecimal emi = calculationService.calculateEmi(principal, annualInterest, tenure);

        // EMI = 1200 / 12 = 100
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), emi);
    }

    @Test
    public void testEmiCalculation_StandardFormula() {
        EmiCalculationService calculationService = new EmiCalculationService();
        BigDecimal principal = BigDecimal.valueOf(10000.00);
        BigDecimal annualInterest = BigDecimal.valueOf(12.00); // 1% monthly
        int tenure = 12;

        BigDecimal emi = calculationService.calculateEmi(principal, annualInterest, tenure);

        // EMI = P * r * (1+r)^n / ((1+r)^n - 1)
        // P = 10000, r = 0.01, n = 12
        // EMI = 10000 * 0.01 * (1.01)^12 / ((1.01)^12 - 1)
        // (1.01)^12 = 1.126825
        // EMI = 100 * 1.126825 / 0.126825 = 888.49
        assertEquals(BigDecimal.valueOf(888.49), emi);
    }

    // ─── 3. REPAYMENT SCHEDULE GENERATION TESTS ─────────────────────────────

    @Test
    public void testRepaymentScheduleGeneration() {
        EmiCalculationService calculationService = new EmiCalculationService();
        LoanApplication application = LoanApplication.builder()
                .applicationId("APP123")
                .approvedAmount(BigDecimal.valueOf(10000.00))
                .interestRate(BigDecimal.valueOf(12.00))
                .emiAmount(BigDecimal.valueOf(888.49))
                .tenureMonths(12)
                .build();

        List<LoanRepayment> repayments = calculationService.generateRepaymentSchedule(application);

        assertEquals(12, repayments.size());

        BigDecimal sumPrincipal = BigDecimal.ZERO;
        for (int i = 0; i < repayments.size(); i++) {
            LoanRepayment rep = repayments.get(i);
            assertEquals("APP123", rep.getApplicationId());
            assertEquals(i + 1, rep.getEmiNumber());
            assertEquals(RepaymentStatus.PENDING, rep.getStatus());
            assertEquals(rep.getPrincipalComponent().add(rep.getInterestComponent()), rep.getEmiAmount());
            sumPrincipal = sumPrincipal.add(rep.getPrincipalComponent());
        }

        // The sum of principal components must equal exactly the total approved amount
        assertEquals(BigDecimal.valueOf(10000.00).setScale(2), sumPrincipal.setScale(2));
    }

    // ─── 4. LOAN APPLICATION PROCESS TESTS ───────────────────────────────────

    @Test
    public void testApplyForLoan_Approved() {
        BigDecimal requested = BigDecimal.valueOf(10000.00);
        int mockScore = 800;

        when(creditScoringService.generateMockScore(userId)).thenReturn(mockScore);
        when(creditScoringService.scoreToCreditDecision(mockScore, requested)).thenReturn(
                CreditScoringService.CreditDecision.builder()
                        .approved(true)
                        .approvedAmount(requested)
                        .interestRate(BigDecimal.valueOf(12.00))
                        .build()
        );
        when(applicationRepository.save(any(LoanApplication.class))).thenAnswer(i -> i.getArgument(0));

        LoanApplication app = loanService.applyForLoan(userId, userUpiId, LoanType.PERSONAL_12MONTH, requested);

        assertNotNull(app);
        assertEquals(LoanStatus.APPROVED, app.getStatus());
        assertEquals(requested, app.getApprovedAmount());
        assertEquals(BigDecimal.valueOf(12.00), app.getInterestRate());
        assertEquals(BigDecimal.valueOf(200.00).setScale(2), app.getProcessingFee()); // 2% of 10000 = 200
        verify(repaymentRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void testApplyForLoan_Rejected() {
        BigDecimal requested = BigDecimal.valueOf(10000.00);
        int mockScore = 600;

        when(creditScoringService.generateMockScore(userId)).thenReturn(mockScore);
        when(creditScoringService.scoreToCreditDecision(mockScore, requested)).thenReturn(
                CreditScoringService.CreditDecision.builder()
                        .approved(false)
                        .approvedAmount(BigDecimal.ZERO)
                        .interestRate(BigDecimal.ZERO)
                        .rejectionReason("CIBIL score is too low")
                        .build()
        );
        when(applicationRepository.save(any(LoanApplication.class))).thenAnswer(i -> i.getArgument(0));

        LoanApplication app = loanService.applyForLoan(userId, userUpiId, LoanType.PERSONAL_12MONTH, requested);

        assertNotNull(app);
        assertEquals(LoanStatus.REJECTED, app.getStatus());
        assertEquals(BigDecimal.ZERO, app.getApprovedAmount());
        assertEquals("CIBIL score is too low", app.getRejectionReason());
        verify(repaymentRepository, never()).saveAll(anyList());
    }

    // ─── 5. LOAN DISBURSEMENT PROCESS TESTS ───────────────────────────────────

    @Test
    public void testDisburseLoan_Success() {
        String appId = "APP123";
        LoanApplication application = LoanApplication.builder()
                .applicationId(appId)
                .userId(userId)
                .userUpiId(userUpiId)
                .approvedAmount(BigDecimal.valueOf(10000.00))
                .processingFee(BigDecimal.valueOf(200.00))
                .status(LoanStatus.APPROVED)
                .build();

        when(applicationRepository.findByApplicationId(appId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(LoanApplication.class))).thenAnswer(i -> i.getArgument(0));

        TransactionResponse txnResponse = TransactionResponse.builder()
                .transactionId("TXN555")
                .status(TransactionStatus.SUCCESS)
                .build();
        NpciApiResponse<TransactionResponse> apiResponse = NpciApiResponse.<TransactionResponse>builder()
                .success(true)
                .data(txnResponse)
                .build();

        when(npciServiceClient.initiateTransaction(anyString(), any(InitiateTransactionRequest.class)))
                .thenReturn(apiResponse);

        LoanApplication result = loanService.disburseLoan(appId);

        assertNotNull(result);
        assertEquals(LoanStatus.DISBURSED, result.getStatus());
        assertNotNull(result.getDisbursedAt());

        ArgumentCaptor<InitiateTransactionRequest> captor = ArgumentCaptor.forClass(InitiateTransactionRequest.class);
        verify(npciServiceClient).initiateTransaction(anyString(), captor.capture());
        assertEquals(BigDecimal.valueOf(9800.00), captor.getValue().getAmount()); // Disbursed = 10000 - 200 = 9800
    }

    // ─── 6. EMI REPAYMENT WORKFLOW TESTS ─────────────────────────────────────

    @Test
    public void testProcessEmiRepayment_MarksPaidAndCloses() {
        String appId = "APP123";
        LoanApplication application = LoanApplication.builder()
                .applicationId(appId)
                .status(LoanStatus.DISBURSED)
                .build();

        LoanRepayment repayment = LoanRepayment.builder()
                .repaymentId("REP999")
                .applicationId(appId)
                .emiNumber(1)
                .emiAmount(BigDecimal.valueOf(1000.00))
                .status(RepaymentStatus.PENDING)
                .build();

        when(applicationRepository.findByApplicationId(appId)).thenReturn(Optional.of(application));
        when(repaymentRepository.findByApplicationIdAndEmiNumber(appId, 1)).thenReturn(Optional.of(repayment));
        when(repaymentRepository.save(any(LoanRepayment.class))).thenAnswer(i -> i.getArgument(0));
        when(repaymentRepository.findByApplicationId(appId)).thenReturn(Arrays.asList(repayment));

        LoanRepayment result = loanService.processEmiRepayment(appId, 1, "TXN777");

        assertNotNull(result);
        assertEquals(RepaymentStatus.PAID, result.getStatus());
        assertEquals("TXN777", result.getTransactionId());
        assertNotNull(result.getPaidAt());

        // Since it was the only repayment, the loan should now close
        verify(applicationRepository).save(application);
        assertEquals(LoanStatus.CLOSED, application.getStatus());
        assertNotNull(application.getClosedAt());
    }

    // ─── 7. OVERDUE EMI ACCRUING PENALTY TESTS ───────────────────────────────

    @Test
    public void testCheckOverdueEmis_AccruesPenaltyAndDefaults() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        LoanRepayment overdueRep = LoanRepayment.builder()
                .repaymentId("REP888")
                .applicationId("APP123")
                .emiNumber(1)
                .emiAmount(BigDecimal.valueOf(100.00))
                .dueDate(lastMonth)
                .status(RepaymentStatus.PENDING)
                .build();

        LoanApplication application = LoanApplication.builder()
                .applicationId("APP123")
                .status(LoanStatus.DISBURSED)
                .build();

        when(repaymentRepository.findByStatusAndDueDateBefore(eq(RepaymentStatus.PENDING), any(LocalDate.class)))
                .thenReturn(Arrays.asList(overdueRep));

        // Let's pretend today is 1 month past the due date (30 days)
        // Overdue status check will find REP888. It will mark it as OVERDUE.
        // Then the overdue list check retrieves all OVERDUE repayments.
        when(repaymentRepository.findByStatusAndDueDateBefore(eq(RepaymentStatus.OVERDUE), any(LocalDate.class)))
                .thenReturn(Arrays.asList(overdueRep));

        // Let's mock a case where it has been overdue for 95 days to verify default logic
        LocalDate ninetyFiveDaysAgo = LocalDate.now().minusDays(95);
        overdueRep.setDueDate(ninetyFiveDaysAgo);

        when(applicationRepository.findByApplicationId("APP123")).thenReturn(Optional.of(application));

        loanService.checkOverdueEmis();

        assertEquals(RepaymentStatus.OVERDUE, overdueRep.getStatus());
        // Penalty = 100 * 0.02 * 95 = 190
        assertEquals(BigDecimal.valueOf(190.00).setScale(2), overdueRep.getPenaltyAmount());
        assertEquals(LoanStatus.DEFAULTED, application.getStatus());
    }
}
