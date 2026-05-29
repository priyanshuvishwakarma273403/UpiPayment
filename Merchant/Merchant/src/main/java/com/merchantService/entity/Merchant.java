package com.merchantService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ================================================================
 * Merchant Entity - MySQL Table: merchants
 * ================================================================
 * Merchant ek business hai jo UPI payments accept karta hai.
 * Merchant registration ke baad:
 * 1. Unique merchant UPI ID milta hai: merchantCode@upimesh
 * 2. QR code generate hota hai (ZXing library se)
 * 3. Settlement account configure hota hai
 * ================================================================
 */
@Entity
@Table(name = "merchants",
        indexes = {
                @Index(name = "idx_merchant_email",  columnList = "email",       unique = true),
                @Index(name = "idx_merchant_upi_id", columnList = "merchant_upi_id", unique = true),
                @Index(name = "idx_merchant_code",   columnList = "merchant_code",   unique = true)
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_code", nullable = false, unique = true, length = 20)
    private String merchantCode;       // e.g. MERCH00123

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "merchant_upi_id", unique = true, length = 60)
    private String merchantUpiId;      // merchantCode@upimesh

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", length = 50)
    private BusinessType businessType;

    @Column(name = "gstin", length = 20)
    private String gstin;              // GST number (optional)

    @Column(name = "bank_account_number", length = 20)
    private String bankAccountNumber;  // Settlement account

    @Column(name = "bank_ifsc", length = 15)
    private String bankIfsc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MerchantStatus status = MerchantStatus.PENDING_VERIFICATION;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = false;  // Verification ke baad true hoga

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BusinessType {
        RETAIL, RESTAURANT, ECOMMERCE, SERVICES, HEALTHCARE, EDUCATION, OTHER
    }

    public enum MerchantStatus {
        PENDING_VERIFICATION, ACTIVE, SUSPENDED, CLOSED
    }

}
