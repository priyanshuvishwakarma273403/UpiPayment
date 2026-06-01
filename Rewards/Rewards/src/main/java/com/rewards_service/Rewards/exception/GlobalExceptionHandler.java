package com.rewards_service.Rewards.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{

    @ExceptionHandler(RewardsException.class)
    public ResponseEntity<Map<String,Object>> handle(RewardsException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of(
                "success",false,
                "errorCode","REWARDS_ERROR",
                "message",ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGeneric(Exception ex) {
        log.error("Rewards error: ",ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success",false,"message","Internal server error","timestamp",LocalDateTime.now().toString()));
    }
}
