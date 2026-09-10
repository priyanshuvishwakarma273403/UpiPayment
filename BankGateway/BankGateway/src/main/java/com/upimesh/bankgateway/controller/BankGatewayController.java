package com.upimesh.bankgateway.controller;

import com.upimesh.bankgateway.model.request.BalanceCheckRequest;
import com.upimesh.bankgateway.model.request.LinkBankAccountRequest;
import com.upimesh.bankgateway.model.response.ApiResponse;
import com.upimesh.bankgateway.model.response.BalanceResponse;
import com.upimesh.bankgateway.model.response.IfscResponse;
import com.upimesh.bankgateway.model.response.LinkedAccountResponse;
import com.upimesh.bankgateway.model.response.UpiResolveResponse;
import com.upimesh.bankgateway.service.BankAccountService;
import com.upimesh.bankgateway.service.IfscService;
import com.upimesh.bankgateway.service.UpiResolveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bank")
@RequiredArgsConstructor
@Slf4j
public class BankGatewayController {

    private final BankAccountService bankAccountService;
    private final IfscService ifscService;
    private final UpiResolveService upiResolveService;

    @PostMapping("/accounts/link")
    public ApiResponse<LinkedAccountResponse> linkBankAccount(
            @Valid @RequestBody LinkBankAccountRequest request) {
        log.info("REST request to link bank account | user={}", request.getUserUpiId());
        LinkedAccountResponse response = bankAccountService.linkBankAccount(request);
        return ApiResponse.success(response, "Bank account successfully linked");
    }

    @GetMapping("/accounts/{userUpiId}")
    public ApiResponse<List<LinkedAccountResponse>> getLinkedAccounts(
            @PathVariable String userUpiId) {
        log.info("REST request to fetch linked accounts | user={}", userUpiId);
        List<LinkedAccountResponse> response = bankAccountService.getLinkedAccounts(userUpiId);
        return ApiResponse.success(response, "Linked accounts retrieved successfully");
    }

    @PostMapping("/accounts/balance")
    public ApiResponse<BalanceResponse> checkBalance(
            @Valid @RequestBody BalanceCheckRequest request) {
        log.info("REST request to check balance | accountId={}", request.getAccountId());
        BalanceResponse response = bankAccountService.checkBalance(request);
        return ApiResponse.success(response, "Balance retrieved successfully");
    }

    @PutMapping("/accounts/{accountId}/set-primary")
    public ApiResponse<LinkedAccountResponse> setPrimaryAccount(
            @PathVariable String accountId,
            @RequestParam String userUpiId) {
        log.info("REST request to set primary account | accountId={} | user={}", accountId, userUpiId);
        LinkedAccountResponse response = bankAccountService.setPrimaryAccount(accountId, userUpiId);
        return ApiResponse.success(response, "Primary account updated successfully");
    }

    @DeleteMapping("/accounts/{accountId}")
    public ApiResponse<String> deactivateAccount(
            @PathVariable String accountId,
            @RequestParam String userUpiId) {
        log.info("REST request to deactivate account | accountId={} | user={}", accountId, userUpiId);
        bankAccountService.deactivateAccount(accountId, userUpiId);
        return ApiResponse.success("Account deactivated successfully", "Account deactivated successfully");
    }

    @GetMapping("/ifsc/{ifscCode}")
    public ApiResponse<IfscResponse> getIfscDetails(
            @PathVariable String ifscCode) {
        log.info("REST request to resolve IFSC | code={}", ifscCode);
        IfscResponse response = ifscService.getIfscDetails(ifscCode);
        return ApiResponse.success(response, "IFSC details retrieved successfully");
    }

    @GetMapping("/upi/resolve/{upiHandle}")
    public ApiResponse<UpiResolveResponse> resolveUpiHandle(
            @PathVariable String upiHandle) {
        log.info("REST request to resolve UPI handle | handle={}", upiHandle);
        UpiResolveResponse response = upiResolveService.resolveUpiHandle(upiHandle);
        return ApiResponse.success(response, "UPI handle resolved successfully");
    }

    @GetMapping("/accounts/primary/{merchantUpiId}")
    public org.springframework.http.ResponseEntity<?> getPrimaryAccount(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @PathVariable String merchantUpiId) {
        log.info("REST request to get primary account for merchant: {}", merchantUpiId);
        com.upimesh.bankgateway.model.entity.LinkedBankAccount account = bankAccountService.getPrimaryAccount(merchantUpiId);
        if (account == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        java.util.Map<String, Object> details = java.util.Map.of(
                "accountId", account.getAccountId(),
                "encryptedAccountNumber", account.getEncryptedAccountNumber(),
                "maskedAccountNumber", account.getMaskedAccountNumber(),
                "ifscCode", account.getIfscCode(),
                "bankName", account.getBankName(),
                "bankReferenceToken", account.getBankReferenceToken() != null ? account.getBankReferenceToken() : "TOK_DEFAULT"
        );
        return org.springframework.http.ResponseEntity.ok(details);
    }
}
