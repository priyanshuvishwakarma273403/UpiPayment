package com.upimesh.settlement.exception;

public class MerchantBankDetailsNotFoundException extends RuntimeException {
    public MerchantBankDetailsNotFoundException(String message) {
        super(message);
    }
}
