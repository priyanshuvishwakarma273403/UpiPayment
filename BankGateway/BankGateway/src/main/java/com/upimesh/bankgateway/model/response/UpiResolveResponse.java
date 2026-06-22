package com.upimesh.bankgateway.model.response;

import com.upimesh.bankgateway.model.enums.BankCode;
import com.upimesh.bankgateway.model.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiResolveResponse {

    private String upiHandle;
    private String accountHolderName;
    private BankCode bankCode;
    private String bankName;
    private String maskedAccountNumber;
    private String ifscCode;
    private VerificationStatus verificationStatus;
    private boolean isActive;
    private LocalDateTime lastResolvedAt;
    private boolean fromCache;
}
