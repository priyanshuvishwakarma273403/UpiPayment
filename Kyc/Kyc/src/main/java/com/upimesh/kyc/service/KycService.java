package com.upimesh.kyc.service;

import com.upimesh.kyc.exception.InvalidKycStateException;
import com.upimesh.kyc.exception.KycNotFoundException;
import com.upimesh.kyc.model.entity.KycAuditLog;
import com.upimesh.kyc.model.entity.KycDocument;
import com.upimesh.kyc.model.entity.KycRecord;
import com.upimesh.kyc.model.enums.DocumentType;
import com.upimesh.kyc.model.enums.KycLevel;
import com.upimesh.kyc.model.enums.KycStatus;
import com.upimesh.kyc.model.request.AadhaarOtpRequest;
import com.upimesh.kyc.model.request.AadhaarVerifyRequest;
import com.upimesh.kyc.model.request.PanVerifyRequest;
import com.upimesh.kyc.model.response.AadhaarOtpResponse;
import com.upimesh.kyc.model.response.KycStatusResponse;
import com.upimesh.kyc.repository.KycAuditLogRepository;
import com.upimesh.kyc.repository.KycDocumentRepository;
import com.upimesh.kyc.repository.KycRecordRepository;
import com.upimesh.kyc.util.KycEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService {

    private final KycRecordRepository kycRecordRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final KycAuditLogRepository kycAuditLogRepository;
    private final UidaiService uidaiService;
    private final PanVerificationService panVerificationService;
    private final KycEncryptionUtil encryptionUtil;

    @Value("${kyc.limits.level-0:10000.00}")
    private BigDecimal limitLevel0;

    @Value("${kyc.limits.level-1:100000.00}")
    private BigDecimal limitLevel1;

    @Value("${kyc.limits.level-2:999999999.00}")
    private BigDecimal limitLevel2;

    @Value("${kyc.uidai.otp-expiry-seconds:300}")
    private int otpExpirySeconds;

    @Transactional
    public AadhaarOtpResponse initiateAadhaarOtp(AadhaarOtpRequest request, String ipAddress, String deviceId) {
        log.info("Initiating Aadhaar OTP for userId: {}", request.getUserId());

        // Check if there is an existing record
        KycRecord record = kycRecordRepository.findByUserId(request.getUserId())
                .orElse(null);

        if (record != null && record.getKycLevel() == KycLevel.LEVEL_2 && record.getStatus() == KycStatus.COMPLETED) {
            throw new InvalidKycStateException("KYC is already completed at LEVEL_2");
        }

        String kycId;
        if (record == null) {
            kycId = UUID.randomUUID().toString();
            record = KycRecord.builder()
                    .kycId(kycId)
                    .userId(request.getUserId())
                    .userUpiId(request.getUserUpiId())
                    .kycLevel(KycLevel.LEVEL_0)
                    .status(KycStatus.IN_PROGRESS)
                    .aadhaarVerified(false)
                    .panVerified(false)
                    .faceMatched(false)
                    .monthlyLimit(limitLevel0)
                    .build();
        } else {
            kycId = record.getKycId();
            record.setStatus(KycStatus.IN_PROGRESS);
        }

        // Generate OTP and save to Redis via UidaiService
        String otp = uidaiService.generateOtp(kycId, request.getAadhaarNumber());

        // Update record with masked Aadhaar (optional, will finalize on verify)
        String rawAadhaar = request.getAadhaarNumber();
        record.setMaskedAadhaar("XXXX-XXXX-" + rawAadhaar.substring(8));
        kycRecordRepository.save(record);

        // Audit Log
        audit(kycId, request.getUserId(), "Initiated Aadhaar verification OTP", ipAddress, deviceId);

        return AadhaarOtpResponse.builder()
                .kycId(kycId)
                .message("OTP sent to mobile number registered with Aadhaar (Mock OTP: " + otp + ")")
                .expirySeconds(otpExpirySeconds)
                .build();
    }

    @Transactional
    public KycStatusResponse verifyAadhaarOtp(AadhaarVerifyRequest request, String ipAddress, String deviceId) {
        log.info("Verifying Aadhaar OTP for kycId: {}", request.getKycId());

        KycRecord record = kycRecordRepository.findByKycId(request.getKycId())
                .orElseThrow(() -> new KycNotFoundException("KYC record not found for ID: " + request.getKycId()));

        if (record.isAadhaarVerified()) {
            return mapToResponse(record);
        }

        // Verify OTP and fetch demographics
        UidaiService.UidaiEkycData ekycData = uidaiService.verifyOtpAndFetchDetails(request.getKycId(), request.getOtp());

        // Update Record
        record.setAadhaarVerified(true);
        record.setFullName(ekycData.getFullName());
        record.setDateOfBirth(ekycData.getDateOfBirth());
        record.setAddress(ekycData.getAddress());
        record.setMaskedAadhaar(ekycData.getMaskedAadhaar());
        record.setKycLevel(KycLevel.LEVEL_0);
        record.setMonthlyLimit(limitLevel0);
        kycRecordRepository.save(record);

        // Save Document
        KycDocument doc = KycDocument.builder()
                .kycId(record.getKycId())
                .documentType(DocumentType.AADHAAR)
                .maskedDocumentNumber(ekycData.getMaskedAadhaar())
                .encryptedDocumentNumber(encryptionUtil.encrypt(ekycData.getMaskedAadhaar())) // secure reference
                .status(KycStatus.COMPLETED)
                .issuer("UIDAI")
                .build();
        kycDocumentRepository.save(doc);

        audit(record.getKycId(), record.getUserId(), "Verified Aadhaar via OTP successfully", ipAddress, deviceId);

        return mapToResponse(record);
    }

    @Transactional
    public KycStatusResponse verifyPan(PanVerifyRequest request, String ipAddress, String deviceId) {
        log.info("Verifying PAN: {} for kycId: {}", request.getPanNumber(), request.getKycId());

        KycRecord record = kycRecordRepository.findByKycId(request.getKycId())
                .orElseThrow(() -> new KycNotFoundException("KYC record not found for ID: " + request.getKycId()));

        // Reject PAN before Aadhaar
        if (!record.isAadhaarVerified()) {
            throw new InvalidKycStateException("Aadhaar verification is required before verifying PAN");
        }

        if (record.isPanVerified()) {
            return mapToResponse(record);
        }

        // Verify PAN via PanVerificationService
        PanVerificationService.PanVerificationResult result = panVerificationService.verifyPan(
                request.getPanNumber(), request.getFullName(), request.getDateOfBirth());

        if (!result.isValid()) {
            audit(record.getKycId(), record.getUserId(), "PAN verification failed: " + result.getMessage(), ipAddress, deviceId);
            throw new IllegalArgumentException("PAN verification failed: " + result.getMessage());
        }

        // Update Record
        record.setPanVerified(true);
        record.setEncryptedPan(encryptionUtil.encrypt(request.getPanNumber()));
        
        String maskedPan = request.getPanNumber().substring(0, 2) + "XXXXXX" + request.getPanNumber().substring(8);
        
        // Upgrade level to LEVEL_1
        record.setKycLevel(KycLevel.LEVEL_1);
        record.setMonthlyLimit(limitLevel1);
        
        // If they don't do face-match, it can be considered completed at LEVEL_1
        record.setStatus(KycStatus.COMPLETED);
        kycRecordRepository.save(record);

        // Save Document
        KycDocument doc = KycDocument.builder()
                .kycId(record.getKycId())
                .documentType(DocumentType.PAN)
                .maskedDocumentNumber(maskedPan)
                .encryptedDocumentNumber(encryptionUtil.encrypt(request.getPanNumber()))
                .status(KycStatus.COMPLETED)
                .issuer("NSDL")
                .build();
        kycDocumentRepository.save(doc);

        audit(record.getKycId(), record.getUserId(), "Verified PAN card successfully", ipAddress, deviceId);

        return mapToResponse(record);
    }

    @Transactional
    public KycStatusResponse performFaceMatch(String kycId, String selfieImageBase64, String ipAddress, String deviceId) {
        log.info("Performing face match for kycId: {}", kycId);

        KycRecord record = kycRecordRepository.findByKycId(kycId)
                .orElseThrow(() -> new KycNotFoundException("KYC record not found for ID: " + kycId));

        if (!record.isAadhaarVerified() || !record.isPanVerified()) {
            throw new InvalidKycStateException("Both Aadhaar and PAN verification are required before Face Match");
        }

        if (record.isFaceMatched()) {
            return mapToResponse(record);
        }

        // Mock verification - standard pass in developer mode
        record.setFaceMatched(true);
        record.setKycLevel(KycLevel.LEVEL_2);
        record.setMonthlyLimit(limitLevel2);
        record.setStatus(KycStatus.COMPLETED);
        kycRecordRepository.save(record);

        // Save Document (Face reference)
        KycDocument doc = KycDocument.builder()
                .kycId(record.getKycId())
                .documentType(DocumentType.PASSPORT) // using PASSPORT or mock placeholder
                .maskedDocumentNumber("FACE-MATCHED")
                .encryptedDocumentNumber(encryptionUtil.encrypt(selfieImageBase64.substring(0, Math.min(selfieImageBase64.length(), 100))))
                .status(KycStatus.COMPLETED)
                .issuer("INTERNAL-BIOMETRIC")
                .build();
        kycDocumentRepository.save(doc);

        audit(record.getKycId(), record.getUserId(), "Completed face match & biometrics successfully. KYC upgraded to LEVEL_2", ipAddress, deviceId);

        return mapToResponse(record);
    }

    public KycStatusResponse getKycStatus(String userId) {
        KycRecord record = kycRecordRepository.findByUserId(userId)
                .orElse(null);

        if (record == null) {
            return KycStatusResponse.builder()
                    .userId(userId)
                    .kycLevel(KycLevel.LEVEL_0)
                    .status(KycStatus.NOT_STARTED)
                    .monthlyLimit(limitLevel0)
                    .build();
        }

        return mapToResponse(record);
    }

    public KycStatusResponse getKycStatusByUpiId(String userUpiId) {
        KycRecord record = kycRecordRepository.findByUserUpiId(userUpiId)
                .orElse(null);

        if (record == null) {
            return KycStatusResponse.builder()
                    .userUpiId(userUpiId)
                    .kycLevel(KycLevel.LEVEL_0)
                    .status(KycStatus.NOT_STARTED)
                    .monthlyLimit(limitLevel0)
                    .build();
        }

        return mapToResponse(record);
    }

    public List<KycAuditLog> getAuditLogs(String kycId) {
        return kycAuditLogRepository.findByKycId(kycId);
    }

    private void audit(String kycId, String userId, String action, String ipAddress, String deviceId) {
        KycAuditLog logRecord = KycAuditLog.builder()
                .kycId(kycId)
                .userId(userId)
                .action(action)
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .deviceId(deviceId != null ? deviceId : "UNKNOWN")
                .build();
        kycAuditLogRepository.save(logRecord);
    }

    private KycStatusResponse mapToResponse(KycRecord record) {
        String maskedPan = null;
        if (record.getEncryptedPan() != null) {
            try {
                String plainPan = encryptionUtil.decrypt(record.getEncryptedPan());
                maskedPan = plainPan.substring(0, 2) + "XXXXXX" + plainPan.substring(8);
            } catch (Exception e) {
                maskedPan = "XXXXXXXXXX";
            }
        }

        return KycStatusResponse.builder()
                .kycId(record.getKycId())
                .userId(record.getUserId())
                .userUpiId(record.getUserUpiId())
                .kycLevel(record.getKycLevel())
                .status(record.getStatus())
                .aadhaarVerified(record.isAadhaarVerified())
                .panVerified(record.isPanVerified())
                .faceMatched(record.isFaceMatched())
                .maskedAadhaar(record.getMaskedAadhaar())
                .maskedPan(maskedPan)
                .fullName(record.getFullName())
                .monthlyLimit(record.getMonthlyLimit())
                .build();
    }
}
