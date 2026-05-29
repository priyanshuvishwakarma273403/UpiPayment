package com.paymentService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaymentException extends RuntimeException {

    private final HttpStatus status;
    public final String errorCode;

    public PaymentException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "PAYMENT_ERROR";
    }

    public PaymentException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "PAYMENT_ERROR";
    }

    public PaymentException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
