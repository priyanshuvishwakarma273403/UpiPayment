package com.upimesh.payroll.feign.dto;

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
