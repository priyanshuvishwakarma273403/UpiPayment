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
 * LinkedBankAccount — A bank account that user has linked to their UPI ID.
 *
 * One user can link multiple bank accounts (e.g., HDFC savings + SBI salary).
 * Each account gets linked to their UPI ID after bank verification.
 *
 * Security: account number is stored ENCRYPTED (AES-256) — never plain text.
 */
@Entity
@Table(name = "linked_bank_accounts", indexes = {
        @Index(name = "idx_user_upi", columnList = "userUpiId"),
        @Index(name = "idx_account_id", columnList = "accountId"),
        @Index(name = "idx_ifsc", columnList = "ifscCode"),
        @Index(name = "idx_bank_status", columnList = "bankCode,status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique link identifier
    @Column(nullable = false, unique = true, length = 40)
    private String accountId;

    // Owner — their UPI ID on our system
    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BankCode bankCode;

    // Encrypted account number (AES-256-GCM)
    // Original: "12345678901234"  →  Stored: "ENC:base64encodedciphertext"
    @Column(nullable = false, length = 512)
    private String encryptedAccounterNumber;

    // Masked account for display: "XXXX XXXX 1234"
    @Column(nullable = false, length = 30)
    private String maskedAccountNumber;

    // IFSC code — e.g. "HDFC0001234"
    @Column(nullable = false, length = 11)
    private String ifscCode;

    @Column(nullable = false, length = 100)
    private String bankName;

    @Column(nullable = false, length = 100)
    private String branchName;

    @Column(nullable = false, length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType accountType;

    // Account holder name as per bank records
    @Column(nullable = false, length = 150)
    private String accountHolderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LinkStatus status = LinkStatus.PENDING;

    // Is this the default/primary account for payments?
    @Builder.Default
    private Boolean isPrimary = false;

    // Bank's reference token for this linked account (for future API calls)
    @Column(length = 200)
    private String bankReferenceToken;

    // When user last verified (for re-verification after 90 days)
    private LocalDateTime lastVerifiedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime linkedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
