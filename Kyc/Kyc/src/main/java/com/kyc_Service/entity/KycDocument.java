package com.kyc_Service.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * KycDocument - MongoDB Collection: kyc_documents
 * Raw API responses aur document metadata store karta hai.
 * MySQL mein sirf verified data rakho, raw data yahan.
 */

@Document(collection = "kyc_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycDocument {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private String documentType;  // AADHAAR, PAN , SELFIE, VIDEO, KYC

    //RAW api response (UIDAI, NSDL , etc)
    private Map<String , Object> apiResponse;

    // Verification details
    private String verificationSource;  // UIDAI / NSDL / DIGILOCKED / MANUAL
    private Boolean isVerified;
    private Double confidenceScore;

    // Document metadata
    private String s3Path;
    private String mimeType;
    private Long fileSizeBytes;

    // Audit
    private String ipAddress;
    private String deviceId;

    @Indexed
    private LocalDateTime createdAt;



}
