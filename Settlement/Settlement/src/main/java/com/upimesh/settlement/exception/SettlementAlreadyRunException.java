package com.upimesh.settlement.exception;

public class SettlementAlreadyRunException extends RuntimeException {
    public SettlementAlreadyRunException(String message) {
        super(message);
    }
}
