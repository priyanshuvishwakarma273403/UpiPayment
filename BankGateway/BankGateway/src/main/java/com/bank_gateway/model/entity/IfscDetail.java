package com.bank_gateway.model.entity;

import com.bank_gateway.model.enums.BankCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * IfscDetail — Local cache of IFSC code → branch information.
 *
 * India has 150,000+ IFSC codes. We cache them locally so we don't
 * hit the external IFSC API on every request.
 *
 * Populated on first lookup, refreshed every 24h via Redis TTL.
 * DB acts as permanent store, Redis as fast lookup cache.
 */
@Entity
@Table(name = "ifsc_details", indexes = {
        @Index(name = "idx_ifsc_code", columnList = "ifscCode", unique = true),
        @Index(name = "idx_bank_code", columnList = "bankCode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IfscDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. "HDFC0001234" — always 11 chars
    @Column(nullable = false, unique = true, length = 11)
    private String ifscCode;

    // First 4 chars identify bank: "HDFC" → HDFC Bank
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BankCode bankCode;

    @Column(nullable = false, length = 100)
    private String bankName;

    @Column(nullable = false, length = 150)
    private String branchName;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(length = 300)
    private String address;

    @Column(length = 20)
    private String contact;

    // Whether this branch supports IMPS, NEFT, RTGS
    @Builder.Default
    private Boolean impsEnabled = true;

    @Builder.Default
    private Boolean neftEnabled = true;

    @Builder.Default
    private Boolean rtgsEnabled = false;   // Not all branches support RTGS

    @CreationTimestamp
    private LocalDateTime cachedAt;



}
