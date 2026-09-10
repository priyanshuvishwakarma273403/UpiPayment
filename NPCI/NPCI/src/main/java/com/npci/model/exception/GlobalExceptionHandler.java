package com.npci.model.exception;

import com.npci.exception.*;
import com.npci.model.response.ApiResponse;
import jakarta.transaction.InvalidTransactionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.TransactionalIdNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — Converts all exceptions to clean API responses.
 * Frontend always gets a consistent JSON structure.
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({TransactionalIdNotFoundException.class, TransactionNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception ex) {
        log.warn("Transaction not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "TXN_NOT_FOUND"));
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalid(InvalidTransactionException ex) {
        log.warn("Invalid transaction: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "INVALID_TRANSACTION"));
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateTransactionException ex) {
        log.warn("Duplicate transaction: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "DUPLICATE_TRANSACTION"));
    }

    @ExceptionHandler(TransactionLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleLimit(TransactionLimitExceededException ex) {
        log.warn("Limit exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage(), "LIMIT_EXCEEDED"));
    }

    @ExceptionHandler(DailyLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleDailyLimit(DailyLimitExceededException ex) {
        log.warn("Daily limit exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage(), "DAILY_LIMIT_EXCEEDED"));
    }

    @ExceptionHandler(NpciCommunicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleNpci(NpciCommunicationException ex) {
        log.error("NPCI communication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(ex.getMessage(), "NPCI_ERROR"));
    }

    // Validation errors (from @Valid annotations)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
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
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }

}
