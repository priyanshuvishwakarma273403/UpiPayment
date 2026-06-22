package com.upimesh.kyc.model.entity;

import com.upimesh.kyc.model.enums.DocumentType;
import com.upimesh.kyc.model.enums.KycStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_documents", indexes = {
        @Index(name = "idx_kyc_document_kyc_id", columnList = "kycId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String kycId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentType documentType;

    @Column(columnDefinition = "TEXT")
    private String encryptedDocumentNumber;

    @Column(length = 50)
    private String maskedDocumentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus status;

    private LocalDate expiryDate;

    @Column(length = 100)
    private String issuer;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
