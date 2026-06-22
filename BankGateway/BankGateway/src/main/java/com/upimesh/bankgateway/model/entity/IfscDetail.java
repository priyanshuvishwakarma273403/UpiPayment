package com.upimesh.bankgateway.model.entity;

import com.upimesh.bankgateway.model.enums.BankCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ifsc_details", indexes = {
        @Index(name = "idx_ifsc_code_unique", columnList = "ifscCode", unique = true),
        @Index(name = "idx_ifsc_bank_code", columnList = "bankCode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IfscDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 11)
    private String ifscCode;

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

    @Builder.Default
    @Column(nullable = false)
    private boolean impsEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean neftEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean rtgsEnabled = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime cachedAt;
}
