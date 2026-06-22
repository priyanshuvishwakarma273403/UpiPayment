package com.upimesh.bankgateway.exception;

import com.upimesh.bankgateway.client.BankClientRegistry;
import com.upimesh.bankgateway.model.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountNotFound(AccountNotFoundException ex) {
        log.warn("Account not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "ACCOUNT_NOT_FOUND"));
    }

    @ExceptionHandler(AccountAlreadyLinkedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountAlreadyLinked(AccountAlreadyLinkedException ex) {
        log.warn("Account already linked: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "ACCOUNT_ALREADY_LINKED"));
    }

    @ExceptionHandler(BankVerificationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBankVerificationFailed(BankVerificationException ex) {
        log.warn("Bank verification failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "BANK_VERIFICATION_FAILED"));
    }

    @ExceptionHandler(InvalidIfscException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidIfsc(InvalidIfscException ex) {
        log.warn("Invalid IFSC: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_IFSC"));
    }

    @ExceptionHandler(MaxAccountsLinkedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxAccountsLinked(MaxAccountsLinkedException ex) {
        log.warn("Max accounts reached: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage(), "MAX_ACCOUNTS_REACHED"));
    }

    @ExceptionHandler(UpiHandleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUpiHandleNotFound(UpiHandleNotFoundException ex) {
        log.warn("UPI handle not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "UPI_HANDLE_NOT_FOUND"));
    }

    @ExceptionHandler(BankClientRegistry.UnsupportedBankException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedBank(BankClientRegistry.UnsupportedBankException ex) {
        log.warn("Unsupported bank: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "UNSUPPORTED_BANK"));
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTransaction(InvalidTransactionException ex) {
        log.warn("Invalid transaction: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_TRANSACTION"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .errorCode("VALIDATION_ERROR")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error in bank gateway: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }
}
