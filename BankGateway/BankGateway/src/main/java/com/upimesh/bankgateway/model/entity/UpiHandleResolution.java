package com.upimesh.bankgateway.model.entity;

import com.upimesh.bankgateway.model.enums.BankCode;
import com.upimesh.bankgateway.model.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "upi_handle_resolutions", indexes = {
        @Index(name = "idx_upi_handle_unique", columnList = "upiHandle", unique = true),
        @Index(name = "idx_resolution_bank_code", columnList = "bankCode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiHandleResolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String upiHandle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BankCode bankCode;

    @Column(nullable = false, length = 150)
    private String accountHolderName;

    @Column(length = 30)
    private String maskedAccountNumber;

    @Column(length = 11)
    private String ifscCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    private LocalDateTime lastResolvedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
