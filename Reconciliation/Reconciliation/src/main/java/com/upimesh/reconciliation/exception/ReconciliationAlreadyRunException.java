package com.upimesh.reconciliation.exception;

public class ReconciliationAlreadyRunException extends RuntimeException {
    public ReconciliationAlreadyRunException(String message) {
        super(message);
    }
}
