package com.upimesh.dispute.feign.dto;

public enum TransactionStatus {
    INITIATED,
    PENDING,
    SUCCESS,
    FAILED,
    TIMEOUT,
    REVERSED,
    REFUND_PENDING,
    DECLINED
}
