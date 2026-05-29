package com.merchantService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MerchantException extends RuntimeException {
    private final HttpStatus status;
    public MerchantException(String message) {
        super(message); this.status = HttpStatus.BAD_REQUEST;
    }
    public MerchantException(String message, HttpStatus status) {
        super(message); this.status = status;
    }
}
