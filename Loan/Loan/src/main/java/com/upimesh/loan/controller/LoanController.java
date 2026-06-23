package com.upimesh.loan.controller;

import com.upimesh.loan.model.entity.LoanApplication;
import com.upimesh.loan.model.entity.LoanRepayment;
import com.upimesh.loan.model.request.EmiRepaymentRequest;
import com.upimesh.loan.model.request.LoanApplyRequest;
import com.upimesh.loan.model.response.ApiResponse;
import com.upimesh.loan.model.response.LoanDetailsResponse;
import com.upimesh.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanService loanService;

    /**
     * POST /loan/apply
     * Applies for a Buy Now Pay Later (BNPL) or instant personal loan.
     */
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanApplication>> applyForLoan(
            @Valid @RequestBody LoanApplyRequest request) {
        log.info("Received loan application request for user: {}", request.getUserId());
        LoanApplication application = loanService.applyForLoan(
                request.getUserId(),
                request.getUserUpiId(),
                request.getLoanType(),
                request.getRequestedAmount()
        );
        String message = application.getStatus() == com.upimesh.loan.model.enums.LoanStatus.APPROVED
                ? "Loan application approved successfully"
                : "Loan application rejected: " + application.getRejectionReason();
        return ResponseEntity.ok(ApiResponse.success(application, message));
    }

    /**
     * GET /loan/{applicationId}
     * Retrieves loan details and repayment schedule.
     */
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<LoanDetailsResponse>> getLoanDetails(
            @PathVariable String applicationId) {
        log.info("Retrieving details for loan: {}", applicationId);
        LoanDetailsResponse details = loanService.getLoanDetails(applicationId);
        return ResponseEntity.ok(ApiResponse.success(details, "Loan details fetched successfully"));
    }

    /**
     * POST /loan/{id}/disburse
     * Disburses loan approved amount to user's UPI ID.
     */
    @PostMapping("/{id}/disburse")
    public ResponseEntity<ApiResponse<LoanApplication>> disburseLoan(
            @PathVariable("id") String applicationId) {
        log.info("Request to disburse loan: {}", applicationId);
        LoanApplication application = loanService.disburseLoan(applicationId);
        return ResponseEntity.ok(ApiResponse.success(application, "Loan amount disbursed successfully"));
    }

    /**
     * POST /loan/{id}/repay
     * Repays a specific EMI installment.
     */
    @PostMapping("/{id}/repay")
    public ResponseEntity<ApiResponse<LoanRepayment>> repayEmi(
            @PathVariable("id") String applicationId,
            @Valid @RequestBody EmiRepaymentRequest request) {
        log.info("Request to repay EMI #{} for loan application: {}", request.getEmiNumber(), applicationId);
        LoanRepayment repayment = loanService.processEmiRepayment(
                applicationId,
                request.getEmiNumber(),
                request.getTransactionId()
        );
        return ResponseEntity.ok(ApiResponse.success(repayment, "EMI payment processed successfully"));
    }

    /**
     * GET /loan/user/{userId}
     * Retrieves all loan applications for a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getUserLoans(
            @PathVariable String userId) {
        log.info("Retrieving loans for user: {}", userId);
        List<LoanApplication> loans = loanService.getUserLoans(userId);
        return ResponseEntity.ok(ApiResponse.success(loans, "User loans retrieved successfully"));
    }
}
