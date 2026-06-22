package com.upimesh.bankgateway.exception;

public class MaxAccountsLinkedException extends RuntimeException {
    public MaxAccountsLinkedException(String message) {
        super(message);
    }
}
