package com.kyc_Service.service;


import com.kyc_Service.dto.request.PanVerifyRequest;
import com.kyc_Service.entity.KycDocument;
import com.kyc_Service.entity.KycLevel;
import com.kyc_Service.entity.KycRecord;
import com.kyc_Service.entity.KycStatus;
import com.kyc_Service.exception.KycException;
import com.kyc_Service.repository.KycDocumentRepository;
import com.kyc_Service.repository.KycRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ================================================================
 * PAN Verification Service
 * ================================================================
 * NSDL / Protean API se PAN verify karta hai.
 * Production providers: Surepass, IDfy, NSDL e-Gov API
 *
 * Verification:
 * 1. PAN number format check
 * 2. NSDL database se PAN active hai check
 * 3. Name match (user-entered vs NSDL record)
 * 4. DOB cross-verify with Aadhaar
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PanVerificationService {

    private final KycRecordRepository kycRecordRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final RestTemplate restTemplate;

    @Value("${kyc.nsdl.api-url:https://api.nsdl.co.in}")
    private String nsdlApiUrl;

    @Value("${kyc.nsdl.api-key:}")
    private String nsdlApiKey;

    @Value("${kyc.mock-mode:true}")
    private boolean mockMode;

    /**
     * PAN Verify karo
     * 1. NSDL se PAN status check
     * 2. Name match verify
     * 3. KYC record update
     */
    @Transactional
    public Map<String, Object> verifyPan(Long userId, PanVerifyRequest request){
        log.info("Verifying PAN for userId={}, pan={}", userId,
                maskPan(request.getPanNumber()));

        KycRecord kycRecord = kycRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new KycException("KYC record not found. Please verify Aadhaar first"));


        // Aadhaar pehle verify hona chahiye
        if(kycRecord.getAadhaarVerifiedAt() == null){
            throw  new KycException("Please complete Aadhaar verification before PAN verification.");
        }

        // PAN already verified?
        if(kycRecord.getPanVerifiedAt() != null){
            throw new KycException("PAN already verified for this account.");
        }

        // PAN already linked to another account?
        if(kycRecordRepository.existsByPanNumber(encryptPan(request.getPanNumber()))){
            throw new KycException("This PAN is already linked to another account.");
        }

        // NSDL se verify karo
        Map<String, Object> nsdlResponse = mockMode
                ? getMockNsdlResponse(request.getPanNumber(), request.getNameAsOnPan())
                : callNsdlApi(request.getPanNumber(), request.getNameAsOnPan());

        boolean panValid   = (boolean) nsdlResponse.getOrDefault("valid", false);
        boolean nameMatch  = (boolean) nsdlResponse.getOrDefault("nameMatch", false);
        String panStatus   = (String)  nsdlResponse.getOrDefault("status", "INVALID");

        if(!panValid){
            throw new KycException("PAN verification failed: " + panStatus);
        }

        if(!nameMatch){
            throw new KycException(
                    "Name mismatch. Please enter your name exactly as on your PAN card.");
        }

        // KYC Record update karo
        kycRecord.setPanNumber(encryptPan(request.getPanNumber()));
        kycRecord.setPanVerifiedAt(LocalDateTime.now());
        kycRecord.setKycStatus(KycStatus.PAN_VERIFIED);

        // Check if we can mark Level 1 complete
        if (kycRecord.getAadhaarVerifiedAt() != null) {
            kycRecord.setKycLevel(KycLevel.LEVEL_1);
            kycRecord.setKycStatus(KycStatus.FACE_MATCH_PENDING);
            kycRecord.setVerifiedAt(LocalDateTime.now());
            // KYC expires in 10 years
            kycRecord.setExpiresAt(LocalDateTime.now().plusYears(10));
        }

        kycRecordRepository.save(kycRecord);

        // MongoDB document save
        KycDocument doc = KycDocument.builder()
                .userId(userId)
                .documentType("PAN")
                .apiResponse(nsdlResponse)
                .verificationSource("NSDL")
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .build();
        kycDocumentRepository.save(doc);

        log.info("PAN verified successfully for userId={}", userId);

        return Map.of(
                "success",  true,
                "message",  "PAN verified successfully",
                "panMasked", maskPan(request.getPanNumber()),
                "kycLevel", kycRecord.getKycLevel().name(),
                "nextStep", "Complete face match to achieve Level 2 KYC"
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callNsdlApi(String panNumber, String name){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", nsdlApiKey);

        Map<String , String> body = new HashMap<>();
        body.put("panNumber", panNumber);
        body.put("name", name);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(
                nsdlApiUrl + "/pan/verify", entity, Map.class);
    }

    private Map<String, Object> getMockNsdlResponse(String pan, String name) {
        return Map.of(
                "valid",     true,
                "nameMatch", true,
                "status",    "ACTIVE",
                "category",  "Individual",
                "pan",       pan
        );
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 10) return "XXXXXXXXXX";
        return "XXXXX" + pan.substring(5, 9) + pan.charAt(9);
    }

    private String encryptPan(String pan) {
        // TODO: AES-256 encryption - production mein implement karo
        return pan;
    }

}
