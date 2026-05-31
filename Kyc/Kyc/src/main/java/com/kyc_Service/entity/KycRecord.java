package com.kyc_Service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ================================================================
 * KycRecord Entity - MySQL Table: kyc_records
 * ================================================================
 * Har user ka KYC status yahan track hota hai.
 *
 * KYC Levels (RBI Guidelines):
 * LEVEL_0 - Phone + OTP only
 *   Limit: ₹10,000/month
 *
 * LEVEL_1 - PAN + Aadhaar (e-KYC)
 *   Limit: ₹1,00,000/month
 *
 * LEVEL_2 - Full KYC (Video KYC ya physical)
 *   Limit: Unlimited
 *
 * Verification Status Flow:
 * PENDING -> AADHAAR_SENT -> AADHAAR_VERIFIED
 *         -> PAN_SUBMITTED -> PAN_VERIFIED
 *         -> FACE_MATCHED -> COMPLETED
 *         -> FAILED / REJECTED
 * ================================================================
 */

@Entity
@Table(name = "kyc_records",
indexes = {
        @Index(name = "idx_kyc_user_id",   columnList = "user_id",   unique = true),
        @Index(name = "idx_kyc_aadhaar",   columnList = "aadhaar_number"),
        @Index(name = "idx_kyc_pan",       columnList = "pan_number"),
        @Index(name = "idx_kyc_status",    columnList = "kyc_status")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_level" , length = 10)
    @Builder.Default
    private KycLevel kycLevel = KycLevel.LEVEL_0;


    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 25)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    // Aadhaar details (masked - only last 4 digits store karo)
    @Column(name = "aadhaar_number", length = 12)
    private String aadhaarNumber;  // encrypted


    @Column(name = "aadhaar_masked" , length = 12)
    private String aadhaarMasked; //    // XXXX-XXXX-1234

    @Column(name = "aadhaar_verified_at")
    private LocalDateTime aadhaarVerifiedAt;

    // PAN details
    @Column(name = "pan_number" , length = 10)
    private String panNumber; // encrypted

    @Column(name = "pan_verified_at")
    private LocalDateTime panVerifiedAt;

    //personal detail from aadhaar/ pan
    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;                 // Encrypted

    // Face match score (0.0 - 1.0)
    @Column(name = "face_match_score")
    private Double faceMatchScore;

    @Column(name = "face_matched_at")
    private LocalDateTime faceMatchedAt;

    // DigiLocker document reference
    @Column(name = "digilocker_uri", length = 500)
    private String digilockerUri;

    // S3 document paths
    @Column(name = "aadhaar_doc_path", length = 500)
    private String aadhaarDocPath;

    @Column(name = "pan_doc_path", length = 500)
    private String panDocPath;

    @Column(name = "selfie_path", length = 500)
    private String selfiePath;

    // Rejection reason
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // Attempt count (max 3 attempts allowed)
    @Column(name = "attempt_count")
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;       // KYC re-verification ke liye

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
