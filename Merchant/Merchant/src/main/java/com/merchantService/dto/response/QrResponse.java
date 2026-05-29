package com.merchantService.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class QrResponse {

    private Long id;
    private String qrReferenceId;
    private Long merchantId;
    private String merchantUpiId;
    private String businessName;
    private String qrType;
    private BigDecimal amount;
    private String description;
    private String upiDeepLink;
    private String qrImageBase64;   // data:image/png;base64,<base64>
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

}
