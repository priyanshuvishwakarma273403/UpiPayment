package com.kyc_Service.service;

import com.kyc_Service.dto.request.AadhaarOtpRequest;
import com.kyc_Service.dto.request.AadhaarVerifyRequest;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * ================================================================
 * Aadhaar Verification Service
 * ================================================================
 * UIDAI (Unique Identification Authority of India) API se
 * Aadhaar verification karta hai.
 *
 * Production mein use karo:
 * - UIDAI Authentication API (eAuth)
 * - DigiLocker API
 * - Third-party: Surepass, IDfy, HyperVerge
 *
 * Flow:
 * 1. Generate OTP -> UIDAI se registered mobile pe OTP aayega
 * 2. Verify OTP -> UIDAI confirm karega
 * 3. Fetch demographic data (name, DOB, address, photo)
 * ================================================================
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class AadhaarVerificationService {

    private final KycRecordRepository kycRecordRepository;
    private final KycDocumentRepository  kycDocumentRepository;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;

    @Value("${kyc.uidai.api-url:https://api.uidai.gov.in}")
    private String uidaiApiUrl;

    @Value("${kyc.uidai.api-key:}")
    private String uidaiApiKey;

    @Value("${kyc.mock-mode:true}")
    private boolean mockMode; // Development mein true

    private static final String OTP_TXN_KEY = "aadhaar_txn:";
    private static final int OTP_EXPIRY_MINUTES = 10;

    /**
     * Step 1: Aadhaar OTP Generate karo
     * UIDAI registered mobile number pe OTP bhejta hai
     */
    @Transactional
    public Map<String , Object> generateAadhaarOtp(Long userId, AadhaarOtpRequest request) {
        log.info("Generating Aadhaar OTP for userId={}", userId);

        // Check: Aadhaar already verified?
        KycRecord kycRecord = kycRecordRepository.findByUserId(userId)
                .orElseGet(() -> createInitialKycRecord(userId));

        if(kycRecord.getAadhaarVerifiedAt() != null){
            throw new KycException("Aadhaar is already verified for this account");
        }

        // Check attempt limit
        if(kycRecord.getAttemptCount() >= 3){
            throw new KycException("Maximum verification attempts exceeded. Please contact support.");
        }

        // Check: Aadhaar already used by another user?
        String maskedAadhaar = maskAadhaar(request.getAadhaarNumber());
        if(kycRecordRepository.existsByAadhaarNumber(encryptAadhaar(request.getAadhaarNumber()))) {
            throw new KycException("This Aadhaar is already linked to another account.");
        }

        // Generate transaction ID
        String txnId = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        // Redis mein txnId -> aadhaarNumber store karo (10 min TTL)
        redisTemplate.opsForValue().set(
                OTP_TXN_KEY + txnId,
                request.getAadhaarNumber(),
                Duration.ofMinutes(OTP_EXPIRY_MINUTES)
        );

        // UIDAI API call (ya mock)
        if(mockMode){
            log.info("[MOCK] Aadhaar OTP sent to registered mobile. txnId={}", txnId);
        }else{
            callUidaiOtpApi(request.getAadhaarNumber(), txnId);
        }

        // KYC status update
        kycRecord.setKycStatus(KycStatus.AADHAAR_OTP_SENT);
        kycRecord.setAttemptCount(kycRecord.getAttemptCount() + 1);
        kycRecordRepository.save(kycRecord);

        return Map.of(
                "success", true,
                "txnId", txnId,
                "message", "OTP sent to Aadhaar registered mobile number",
                "maskedAadhaar", maskedAadhaar,
                "expiresInMinutes", OTP_EXPIRY_MINUTES
        );
    }

    /**
     * Step 2: Aadhaar OTP Verify karo
     * UIDAI se demographic data fetch karo (name, DOB, address, photo)
     */
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 2 , backoff = @Backoff(delay = 1000))
    public Map<String , Object> verifyAadhaarOtp(Long userId, AadhaarVerifyRequest request) {
        log.info("Verifying Aadhaar OTP for userId={}", userId);

        // txnId se aadhaar number retrieve karo
        String storedAadhar = redisTemplate.opsForValue().get(OTP_TXN_KEY + request.getTxnId());
        if(storedAadhar == null){
            throw new KycException("OTP session expired. Please request a new OTP.");
        }

        if(!storedAadhar.equals(request.getAadhaarNumber())){
            throw new KycException("Aadhaar number mismatch.");
        }

        // UIDAI se verify karo + demographic data fetch karo
        Map<String, Object> demographicData = mockMode
                ? getMockDemographicData(request.getAadhaarNumber())
                : callUidaiVerifyApi(request.getAadhaarNumber(), request.getOtp(), request.getTxnId());

        // KYC Record update karo
        KycRecord kycRecord = kycRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new KycException("Kyc record not found."));

        kycRecord.setAadhaarNumber(encryptAadhaar(request.getAadhaarNumber()));
        kycRecord.setAadhaarMasked(maskAadhaar(request.getAadhaarNumber()));
        kycRecord.setFullName((String) demographicData.get("name"));
        kycRecord.setGender((String) demographicData.get("gender"));
        kycRecord.setAadhaarVerifiedAt(LocalDateTime.now());
        kycRecord.setKycStatus(KycStatus.AADHAAR_VERIFIED);
        kycRecord.setKycLevel(KycLevel.LEVEL_1); // Aadhaar verified = Level 1

        kycRecordRepository.save(kycRecord);

        // Redis se OTP session delete karo
        redisTemplate.delete(OTP_TXN_KEY + request.getTxnId());

        // MongoDB mein document record save karo
        saveKycDocument(userId, "AADHAAR", demographicData, "UIDAI", true);

        log.info("Aadhaar verified successfully for userId={}", userId);


        return Map.of(
                "success", true,
                "message", "Aadhaar verified successfully",
                "name", demographicData.get("name"),
                "aadhaarMasked", maskAadhaar(request.getAadhaarNumber()),
                "nextStep", "Please verify your PAN card for Level 1 KYC"
        );
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> callUidaiOtpApi(String aadhaarNumber, String txnId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", uidaiApiKey);

        Map<String, String> body = new HashMap<>();
        body.put("aadhaarNumber", aadhaarNumber);
        body.put("txnId", txnId);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(uidaiApiUrl + "/otp/generate", entity, Map.class);
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> callUidaiVerifyApi(String aadhaarNumber,String otp , String txnId){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", uidaiApiKey);
        Map<String, String> body = new HashMap<>();
        body.put("aadhaarNumber", aadhaarNumber);
        body.put("otp", otp);
        body.put("txnId", txnId);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(uidaiApiUrl + "/otp/verify", entity, Map.class);
    }

    /** Mock demographic data for development */
    private Map<String, Object> getMockDemographicData(String aadhaarNumber){
        return Map.of(
                "name", "Test User",
                "gender", "M",
                "dob", "01/01/1990",
                "address", "123, Test Street, Mumbai, Maharashtra - 400001",
                "photo", "base64_photo_placeholder"
        );
    }

    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 12) return "XXXX-XXXX-XXXX";
        return "XXXX-XXXX-" + aadhaar.substring(8);
    }

    private String encryptAadhaar(String aadhaar) {
//         TODO: AES-256 encryption use karo production mein
        // AbI sirf store karte hain (PLACEHOLDER)
        return aadhaar; // Replace with encrypted value
    }


    private KycRecord createInitialKycRecord(Long userId) {
        KycRecord record = KycRecord.builder()
                .userId(userId)
                .kycLevel(KycLevel.LEVEL_0)
                .kycStatus(KycStatus.PENDING)
                .attemptCount(0)
                .build();
        return kycRecordRepository.save(record);
    }

    private void saveKycDocument(Long userId, String docType, Map<String, Object> apiResponse,
                                 String source, boolean verified) {
        KycDocument doc = KycDocument.builder()
                .userId(userId)
                .documentType(docType)
                .apiResponse(apiResponse)
                .verificationSource(source)
                .isVerified(verified)
                .createdAt(LocalDateTime.now())
                .build();
        kycDocumentRepository.save(doc);
    }

}
