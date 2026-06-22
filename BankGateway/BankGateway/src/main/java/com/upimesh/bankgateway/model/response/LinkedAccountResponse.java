package com.upimesh.bankgateway.model.response;

import com.upimesh.bankgateway.model.enums.AccountType;
import com.upimesh.bankgateway.model.enums.BankCode;
import com.upimesh.bankgateway.model.enums.LinkStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedAccountResponse {

    private String accountId;
    private String userUpiId;
    private BankCode bankCode;
    private String bankName;
    private String maskedAccountNumber;
    private String ifscCode;
    private String branchName;
    private String city;
    private AccountType accountType;
    private String accountHolderName;
    private LinkStatus status;
    private boolean isPrimary;
    private LocalDateTime linkedAt;
}
