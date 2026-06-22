package com.upimesh.bankgateway.exception;

public class BankVerificationException extends RuntimeException {
    public BankVerificationException(String message) {
        super(message);
    }
    public BankVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
