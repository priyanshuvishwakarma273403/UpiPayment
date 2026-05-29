package com.walletService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WalletException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public WalletException(String message){
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = message;
    }

    public WalletException(String message, HttpStatus status){
        super(message);
        this.status = status;
        this.errorCode = "WALLET_ERROR";
    }

    public WalletException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
