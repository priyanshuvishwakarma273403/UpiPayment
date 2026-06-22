package com.upimesh.settlement.exception;

public class SettlementProcessingException extends RuntimeException {
    public SettlementProcessingException(String message) {
        super(message);
    }
    public SettlementProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
