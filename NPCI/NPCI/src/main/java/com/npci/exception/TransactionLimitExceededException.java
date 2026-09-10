package com.npci.exception;

public class TransactionLimitExceededException extends RuntimeException {
    public TransactionLimitExceededException(String message) {
        super(message);
    }
}
