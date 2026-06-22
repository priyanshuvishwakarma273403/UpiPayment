package com.bank_gateway.model.entity;

import com.bank_gateway.model.enums.BankCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * UpiHandleResolution — Maps a UPI handle to underlying bank account details.
 *
 * When user pays to "merchant@hdfc", we need to resolve:
 *   "merchant@hdfc" → HDFC Bank → Account: XXXX1234 → IFSC: HDFC0001234
 *
 * This resolution is cached here after first lookup from bank.
 * Cache TTL: 1 hour (bank accounts don't change frequently)
 */

@Entity
@Table(name = "upi_handle_resolutions", indexes = {
        @Index(name = "idx_upi_handle", columnList = "upiHandle", unique = true),
        @Index(name = "idx_bank_code", columnList = "bankCode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiHandleResolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Full UPI ID: e.g. "9876543210@upimesh" or "merchant@hdfc"
    @Column(nullable = false, unique = true, length = 100)
    private String upiHandle;

    // Derived from handle suffix: "@hdfc" → HDFC
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BankCode bankCode;

    // Name as registered with bank (shown in payment confirmation)
    @Column(nullable = false, length = 150)
    private String accountHolderName;

    // Masked: "XXXX XXXX 5678"
    @Column(length = 30)
    private String maskedAccountNumber;

    @Column(length = 11)
    private String ifscCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    // Is this UPI handle active? (some may be deactivated by bank)
    @Builder.Default
    private Boolean isActive = true;

    // Bank's response timestamp (for cache invalidation logic)
    private LocalDateTime lastResolvedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
