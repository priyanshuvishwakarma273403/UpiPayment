package com.rewards_service.Rewards.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RewardsException extends RuntimeException {

    private final HttpStatus status;
    public RewardsException(String m){
        super(m);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public RewardsException(String m, HttpStatus status){
        super(m);
        this.status = status;
    }


}
