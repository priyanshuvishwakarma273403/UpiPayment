package com.merchantService.dto.request;

import com.merchantService.entity.Merchant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MerchantRegisterRequest {

    @NotBlank(message = "Business name required")
    private String businessName;

    @NotBlank(message = "Owner name required")
    private String ownerName;

    @NotBlank @Email(message = "Valid email required")
    private String email;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Valid 10-digit mobile number required")
    private String phoneNumber;

    @NotNull(message = "Business type required")
    private Merchant.BusinessType businessType;

    private String gstin;
    private String bankAccountNumber;
    private String bankIfsc;

}
