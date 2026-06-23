package com.upimesh.payroll.exception;

public class PayrollBatchNotFoundException extends RuntimeException {
    public PayrollBatchNotFoundException(String message) {
        super(message);
    }
}
