package com.merchantService.dto.response;

import com.merchantService.entity.Merchant;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MerchantResponse {
    private Long id;
    private String merchantCode;
    private String businessName;
    private String ownerName;
    private String email;
    private String phoneNumber;
    private String merchantUpiId;
    private String businessType;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static MerchantResponse fromEntity(Merchant m) {
        return MerchantResponse.builder()
                .id(m.getId())
                .merchantCode(m.getMerchantCode())
                .businessName(m.getBusinessName())
                .ownerName(m.getOwnerName())
                .email(m.getEmail())
                .phoneNumber(m.getPhoneNumber())
                .merchantUpiId(m.getMerchantUpiId())
                .businessType(m.getBusinessType() != null ? m.getBusinessType().name() : null)
                .status(m.getStatus().name())
                .isActive(m.getIsActive())
                .createdAt(m.getCreatedAt())
                .build();
    }

}
