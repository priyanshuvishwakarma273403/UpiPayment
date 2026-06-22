package com.upimesh.bankgateway.model.entity;

import com.upimesh.bankgateway.model.enums.AccountType;
import com.upimesh.bankgateway.model.enums.BankCode;
import com.upimesh.bankgateway.model.enums.LinkStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "linked_bank_accounts", indexes = {
        @Index(name = "idx_user_upi_id", columnList = "userUpiId"),
        @Index(name = "idx_account_id", columnList = "accountId"),
        @Index(name = "idx_ifsc_code", columnList = "ifscCode"),
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

    @Column(nullable = false, unique = true, length = 40)
    private String accountId;

    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BankCode bankCode;

    @Column(nullable = false, length = 512)
    private String encryptedAccountNumber;

    @Column(nullable = false, length = 30)
    private String maskedAccountNumber;

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

    @Column(nullable = false, length = 150)
    private String accountHolderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LinkStatus status = LinkStatus.PENDING;

    @Builder.Default
    private boolean isPrimary = false;

    @Column(length = 200)
    private String bankReferenceToken;

    private LocalDateTime lastVerifiedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime linkedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
