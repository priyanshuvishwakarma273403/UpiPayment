package com.npci.exception;

public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String message) {
        super(message);
    }
}
