package com.npci.exception;

public class TransactionBlockedException extends RuntimeException {
    public TransactionBlockedException(String message) {
        super(message);
    }
}
