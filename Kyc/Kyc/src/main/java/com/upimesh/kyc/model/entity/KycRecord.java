package com.upimesh.kyc.model.entity;

import com.upimesh.kyc.model.enums.KycLevel;
import com.upimesh.kyc.model.enums.KycStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_records", indexes = {
        @Index(name = "idx_kyc_record_user_id", columnList = "userId", unique = true),
        @Index(name = "idx_kyc_record_user_upi_id", columnList = "userUpiId", unique = true),
        @Index(name = "idx_kyc_record_kyc_id", columnList = "kycId", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String kycId;

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycLevel kycLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus status;

    @Column(nullable = false)
    private boolean aadhaarVerified;

    @Column(nullable = false)
    private boolean panVerified;

    @Column(nullable = false)
    private boolean faceMatched;

    @Column(length = 20)
    private String maskedAadhaar;

    @Column(columnDefinition = "TEXT")
    private String encryptedPan;

    @Column(length = 150)
    private String fullName;

    private LocalDate dateOfBirth;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal monthlyLimit;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
