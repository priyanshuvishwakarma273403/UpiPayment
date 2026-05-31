package com.kyc_Service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class KycException extends RuntimeException{
    private final HttpStatus status;
    private final String errorCode;

    public KycException(String message){
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "KYC_ERROR";
    }


    public KycException(String message,HttpStatus status){
        super(message);
        this.status = status;
        this.errorCode = "KYC_ERROR";
    }

    public KycException(String message,HttpStatus status,String errorCode){
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

}
