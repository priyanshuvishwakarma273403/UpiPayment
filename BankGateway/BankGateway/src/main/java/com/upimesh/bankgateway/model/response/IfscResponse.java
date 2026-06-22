package com.upimesh.bankgateway.model.response;

import com.upimesh.bankgateway.model.enums.BankCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IfscResponse {

    private String ifscCode;
    private BankCode bankCode;
    private String bankName;
    private String branchName;
    private String city;
    private String district;
    private String state;
    private String address;
    private String contact;
    private boolean impsEnabled;
    private boolean neftEnabled;
    private boolean rtgsEnabled;
    private boolean fromCache;
}
