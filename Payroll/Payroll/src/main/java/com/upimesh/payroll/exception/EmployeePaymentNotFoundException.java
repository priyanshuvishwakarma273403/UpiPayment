package com.upimesh.payroll.exception;

public class EmployeePaymentNotFoundException extends RuntimeException {
    public EmployeePaymentNotFoundException(String message) {
        super(message);
    }
}
