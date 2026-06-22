package com.upimesh.settlement.exception;

import com.upimesh.settlement.model.response.ApiResponse;
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

    @ExceptionHandler(SettlementNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSettlementNotFound(SettlementNotFoundException ex) {
        log.warn("Settlement not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "SETTLEMENT_NOT_FOUND"));
    }

    @ExceptionHandler(SettlementAlreadyRunException.class)
    public ResponseEntity<ApiResponse<Void>> handleSettlementAlreadyRun(SettlementAlreadyRunException ex) {
        log.warn("Settlement already run: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "SETTLEMENT_ALREADY_RUN"));
    }

    @ExceptionHandler(MerchantBankDetailsNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMerchantBankDetailsNotFound(MerchantBankDetailsNotFoundException ex) {
        log.warn("Merchant bank details not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "MERCHANT_BANK_DETAILS_NOT_FOUND"));
    }

    @ExceptionHandler(SettlementProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handleSettlementProcessing(SettlementProcessingException ex) {
        log.error("Settlement processing exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), "SETTLEMENT_PROCESSING_FAILED"));
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
        log.error("Unexpected error in settlement service: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }
}
