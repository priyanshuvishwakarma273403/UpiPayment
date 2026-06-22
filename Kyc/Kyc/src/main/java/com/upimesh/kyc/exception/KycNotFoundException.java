package com.upimesh.kyc.exception;

public class KycNotFoundException extends RuntimeException {
    public KycNotFoundException(String message) {
        super(message);
    }
}
