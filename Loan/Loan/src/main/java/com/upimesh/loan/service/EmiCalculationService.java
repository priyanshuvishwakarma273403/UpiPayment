package com.upimesh.loan.service;

import com.upimesh.loan.model.entity.LoanApplication;
import com.upimesh.loan.model.entity.LoanRepayment;
import com.upimesh.loan.model.enums.RepaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class EmiCalculationService {

    /**
     * Calculates the Equated Monthly Installment (EMI) using the standard formula:
     * EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
     */
    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0 || tenureMonths <= 0) {
            return BigDecimal.ZERO;
        }

        double monthlyRate = annualInterestRate.doubleValue() / 12.0 / 100.0;
        if (monthlyRate == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        double p = principal.doubleValue();
        double n = tenureMonths;

        double emi = (p * monthlyRate * Math.pow(1 + monthlyRate, n)) / (Math.pow(1 + monthlyRate, n) - 1);
        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Generates a detailed repayment schedule for a loan application.
     */
    public List<LoanRepayment> generateRepaymentSchedule(LoanApplication loanApplication) {
        log.info("Generating repayment schedule for application: {}", loanApplication.getApplicationId());

        BigDecimal remainingPrincipal = loanApplication.getApprovedAmount();
        BigDecimal emi = loanApplication.getEmiAmount();
        BigDecimal annualRate = loanApplication.getInterestRate();
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        int tenure = loanApplication.getTenureMonths();

        List<LoanRepayment> repayments = new ArrayList<>();
        LocalDate baseDueDate = LocalDate.now().plusMonths(1); // First EMI starts next month

        for (int i = 1; i <= tenure; i++) {
            BigDecimal interest;
            BigDecimal principal;
            BigDecimal currentEmi = emi;

            if (i == tenure) {
                // Adjust final payment to exactly match remaining principal due to roundings
                principal = remainingPrincipal;
                interest = remainingPrincipal.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                currentEmi = principal.add(interest);
            } else {
                interest = remainingPrincipal.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                principal = currentEmi.subtract(interest).setScale(2, RoundingMode.HALF_UP);
                remainingPrincipal = remainingPrincipal.subtract(principal);
            }

            String repaymentId = "REP" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 15).toUpperCase();

            LoanRepayment repayment = LoanRepayment.builder()
                    .repaymentId(repaymentId)
                    .applicationId(loanApplication.getApplicationId())
                    .emiNumber(i)
                    .emiAmount(currentEmi)
                    .principalComponent(principal)
                    .interestComponent(interest)
                    .dueDate(baseDueDate.plusMonths(i - 1))
                    .status(RepaymentStatus.PENDING)
                    .penaltyAmount(BigDecimal.ZERO)
                    .build();

            repayments.add(repayment);
        }

        return repayments;
    }

    /**
     * Calculates penalty for an overdue repayment: 2% of EMI per day overdue.
     */
    public BigDecimal calculatePenalty(LoanRepayment repayment) {
        if (repayment.getStatus() != RepaymentStatus.OVERDUE && repayment.getStatus() != RepaymentStatus.PENDING) {
            return BigDecimal.ZERO;
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(repayment.getDueDate())) {
            return BigDecimal.ZERO;
        }

        long daysOverdue = ChronoUnit.DAYS.between(repayment.getDueDate(), today);
        if (daysOverdue <= 0) {
            return BigDecimal.ZERO;
        }

        // 2% per day of the EMI amount
        BigDecimal penaltyPerDay = repayment.getEmiAmount().multiply(BigDecimal.valueOf(0.02));
        return penaltyPerDay.multiply(BigDecimal.valueOf(daysOverdue)).setScale(2, RoundingMode.HALF_UP);
    }
}
