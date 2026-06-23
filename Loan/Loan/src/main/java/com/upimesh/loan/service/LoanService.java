package com.upimesh.loan.service;

import com.upimesh.loan.exception.LoanApplicationNotFoundException;
import com.upimesh.loan.exception.RepaymentNotFoundException;
import com.upimesh.loan.feign.NpciServiceClient;
import com.upimesh.loan.feign.dto.InitiateTransactionRequest;
import com.upimesh.loan.feign.dto.NpciApiResponse;
import com.upimesh.loan.feign.dto.TransactionResponse;
import com.upimesh.loan.feign.dto.TransactionStatus;
import com.upimesh.loan.feign.dto.TransactionType;
import com.upimesh.loan.model.entity.LoanApplication;
import com.upimesh.loan.model.entity.LoanRepayment;
import com.upimesh.loan.model.enums.LoanStatus;
import com.upimesh.loan.model.enums.LoanType;
import com.upimesh.loan.model.enums.RepaymentStatus;
import com.upimesh.loan.model.response.LoanDetailsResponse;
import com.upimesh.loan.repository.LoanApplicationRepository;
import com.upimesh.loan.repository.LoanRepaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanApplicationRepository applicationRepository;
    private final LoanRepaymentRepository repaymentRepository;
    private final CreditScoringService creditScoringService;
    private final EmiCalculationService emiCalculationService;
    private final NpciServiceClient npciServiceClient;

    @Value("${loan.internal-key:internal-secret-change-in-prod}")
    private String internalKey;

    /**
     * Applies for a loan. Performs a credit check, evaluates interest and approval amount,
     * calculates EMI, and generates a repayment schedule.
     */
    @Transactional
    public LoanApplication applyForLoan(String userId, String userUpiId, LoanType loanType, BigDecimal requestedAmount) {
        log.info("Processing loan application for user: {} | type: {} | amount: ₹{}", userId, loanType, requestedAmount);

        // Generate unique application ID
        String applicationId = "APP" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();

        // 1. Credit Check
        int creditScore = creditScoringService.generateMockScore(userId);
        CreditScoringService.CreditDecision decision = creditScoringService.scoreToCreditDecision(creditScore, requestedAmount);

        if (!decision.isApproved()) {
            log.warn("Loan application rejected for user: {} due to low credit score: {}", userId, creditScore);
            LoanApplication rejectedApp = LoanApplication.builder()
                    .applicationId(applicationId)
                    .userId(userId)
                    .userUpiId(userUpiId)
                    .loanType(loanType)
                    .requestedAmount(requestedAmount)
                    .approvedAmount(BigDecimal.ZERO)
                    .interestRate(BigDecimal.ZERO)
                    .processingFee(BigDecimal.ZERO)
                    .tenureMonths(loanType.getTenureMonths())
                    .emiAmount(BigDecimal.ZERO)
                    .status(LoanStatus.REJECTED)
                    .creditScore(creditScore)
                    .rejectionReason(decision.getRejectionReason())
                    .build();
            return applicationRepository.save(rejectedApp);
        }

        // 2. Adjust Rate for BNPL (Interest-free for 30-day BNPL)
        BigDecimal interestRate = decision.getInterestRate();
        if (loanType == LoanType.BNPL_30DAY) {
            interestRate = BigDecimal.ZERO;
        }

        // 3. Compute Processing Fee (1% for BNPL, 2% for Personal Loans)
        BigDecimal feePercent = loanType.isBnpl() ? BigDecimal.valueOf(0.01) : BigDecimal.valueOf(0.02);
        BigDecimal processingFee = decision.getApprovedAmount().multiply(feePercent)
                .setScale(2, RoundingMode.HALF_UP);

        int tenure = loanType.getTenureMonths();

        // 4. Calculate EMI
        BigDecimal emi = emiCalculationService.calculateEmi(decision.getApprovedAmount(), interestRate, tenure);

        LoanApplication approvedApp = LoanApplication.builder()
                .applicationId(applicationId)
                .userId(userId)
                .userUpiId(userUpiId)
                .loanType(loanType)
                .requestedAmount(requestedAmount)
                .approvedAmount(decision.getApprovedAmount())
                .interestRate(interestRate)
                .processingFee(processingFee)
                .tenureMonths(tenure)
                .emiAmount(emi)
                .status(LoanStatus.APPROVED)
                .creditScore(creditScore)
                .build();

        approvedApp = applicationRepository.save(approvedApp);

        // 5. Generate Repayment Schedule
        List<LoanRepayment> repayments = emiCalculationService.generateRepaymentSchedule(approvedApp);
        repaymentRepository.saveAll(repayments);

        log.info("Loan application approved. ID: {} | Approved Amount: ₹{} | Monthly EMI: ₹{}", 
                applicationId, approvedApp.getApprovedAmount(), emi);

        return approvedApp;
    }

    /**
     * Disbursements of an approved loan via UPI (NPCI integration).
     */
    @Transactional
    public LoanApplication disburseLoan(String applicationId) {
        log.info("Disbursing loan for application: {}", applicationId);

        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found: " + applicationId));

        if (application.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Loan application must be in APPROVED status for disbursement. Current status: " 
                    + application.getStatus());
        }

        // Disbursable Amount = Approved Amount - Processing Fee
        BigDecimal disburseAmount = application.getApprovedAmount().subtract(application.getProcessingFee());

        InitiateTransactionRequest request = InitiateTransactionRequest.builder()
                .senderUpiId("lending@upimesh")
                .receiverUpiId(application.getUserUpiId())
                .amount(disburseAmount)
                .remarks("Loan Disburse " + applicationId)
                .type(TransactionType.P2P)
                .deviceId("LendingServiceServer")
                .ipAddress("127.0.0.1")
                .mpinHash("lending-service-mpin-hash")
                .idempotencyKey("DISB-" + applicationId)
                .build();

        try {
            NpciApiResponse<TransactionResponse> apiResponse = npciServiceClient.initiateTransaction(internalKey, request);
            if (apiResponse == null || !apiResponse.isSuccess() || apiResponse.getData() == null) {
                String msg = apiResponse != null ? apiResponse.getMessage() : "Null response from NPCI";
                log.error("NPCI transaction initiation failed: {}", msg);
                throw new RuntimeException("Disbursement failed via NPCI: " + msg);
            }

            TransactionResponse txn = apiResponse.getData();
            if (txn.getStatus() != TransactionStatus.SUCCESS) {
                log.error("Disbursement transaction was not successful: {}", txn.getStatus());
                throw new RuntimeException("Disbursement transaction declined via NPCI. Status: " + txn.getStatus());
            }

            // Update application status
            application.setStatus(LoanStatus.DISBURSED);
            application.setDisbursedAt(LocalDateTime.now());
            application = applicationRepository.save(application);

            log.info("Loan disbursed successfully for application: {}. Transaction ID: {}", applicationId, txn.getTransactionId());
            return application;

        } catch (Exception e) {
            log.error("Exception during loan disbursement for application: {}", applicationId, e);
            throw new RuntimeException("Failed to disburse loan via NPCI: " + e.getMessage(), e);
        }
    }

    /**
     * Processes EMI repayment.
     */
    @Transactional
    public LoanRepayment processEmiRepayment(String applicationId, int emiNumber, String transactionId) {
        log.info("Processing repayment for loan: {} | EMI #: {} | Txn: {}", applicationId, emiNumber, transactionId);

        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found: " + applicationId));

        LoanRepayment repayment = repaymentRepository.findByApplicationIdAndEmiNumber(applicationId, emiNumber)
                .orElseThrow(() -> new RepaymentNotFoundException("Repayment schedule not found for EMI #" + emiNumber));

        if (repayment.getStatus() == RepaymentStatus.PAID) {
            log.warn("EMI #{} for loan {} is already marked as PAID.", emiNumber, applicationId);
            return repayment;
        }

        // Update Repayment
        repayment.setStatus(RepaymentStatus.PAID);
        repayment.setPaidAt(LocalDateTime.now());
        repayment.setTransactionId(transactionId);
        repayment = repaymentRepository.save(repayment);

        // Check if all EMIs for this loan are paid
        List<LoanRepayment> repayments = repaymentRepository.findByApplicationId(applicationId);
        boolean allPaid = repayments.stream().allMatch(r -> r.getStatus() == RepaymentStatus.PAID);

        if (allPaid) {
            application.setStatus(LoanStatus.CLOSED);
            application.setClosedAt(LocalDateTime.now());
            applicationRepository.save(application);
            log.info("All EMIs paid. Loan application {} is now CLOSED.", applicationId);
        }

        return repayment;
    }

    /**
     * Scheduled task run daily to check for past due PENDING EMIs, marks them as OVERDUE and accrues penalty.
     */
    @Scheduled(cron = "0 0 1 * * ?") // 1:00 AM daily
    @Transactional
    public void checkOverdueEmis() {
        log.info("Scheduled task running: checking overdue EMIs");
        LocalDate today = LocalDate.now();

        // 1. Find PENDING EMIs past due date
        List<LoanRepayment> pendingOverdue = repaymentRepository.findByStatusAndDueDateBefore(RepaymentStatus.PENDING, today);
        for (LoanRepayment repayment : pendingOverdue) {
            repayment.setStatus(RepaymentStatus.OVERDUE);
            repaymentRepository.save(repayment);
            log.info("Repayment {} marked as OVERDUE.", repayment.getRepaymentId());
        }

        // 2. Recalculate penalties for all OVERDUE repayments
        List<LoanRepayment> allOverdue = repaymentRepository.findByStatusAndDueDateBefore(RepaymentStatus.OVERDUE, today.plusDays(1));
        for (LoanRepayment repayment : allOverdue) {
            BigDecimal penalty = emiCalculationService.calculatePenalty(repayment);
            repayment.setPenaltyAmount(penalty);
            repaymentRepository.save(repayment);

            // If repayment is overdue by >90 days, mark the loan application itself as DEFAULTED
            long daysOverdue = ChronoUnit.DAYS.between(repayment.getDueDate(), today);
            if (daysOverdue > 90) {
                applicationRepository.findByApplicationId(repayment.getApplicationId()).ifPresent(app -> {
                    if (app.getStatus() == LoanStatus.DISBURSED) {
                        app.setStatus(LoanStatus.DEFAULTED);
                        applicationRepository.save(app);
                        log.warn("Loan application {} marked as DEFAULTED due to 90+ days overdue repayment.", app.getApplicationId());
                    }
                });
            }
        }
    }

    public List<LoanApplication> getUserLoans(String userId) {
        return applicationRepository.findByUserId(userId);
    }

    public LoanDetailsResponse getLoanDetails(String applicationId) {
        LoanApplication application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found: " + applicationId));

        List<LoanRepayment> schedule = repaymentRepository.findByApplicationId(applicationId);
        // Sort schedule by EMI number
        Collections.sort(schedule, (r1, r2) -> Integer.compare(r1.getEmiNumber(), r2.getEmiNumber()));

        return LoanDetailsResponse.builder()
                .application(application)
                .schedule(schedule)
                .build();
    }
}
