package com.upimesh.payroll.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiResolveResponse {
    private String upiHandle;
    private String accountHolderName;
    private String bankName;
    private String maskedAccountNumber;
    private String ifscCode;
    private String verificationStatus;
    private boolean isActive;
}
