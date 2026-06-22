package com.upimesh.bankgateway.exception;

public class AccountAlreadyLinkedException extends RuntimeException {
    public AccountAlreadyLinkedException(String message) {
        super(message);
    }
}
