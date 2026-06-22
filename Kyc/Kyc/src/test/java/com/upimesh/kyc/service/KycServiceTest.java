package com.upimesh.kyc.service;

import com.upimesh.kyc.exception.InvalidKycStateException;
import com.upimesh.kyc.exception.KycNotFoundException;
import com.upimesh.kyc.model.entity.KycRecord;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KycServiceTest {

    @Mock
    private KycRecordRepository kycRecordRepository;

    @Mock
    private KycDocumentRepository kycDocumentRepository;

    @Mock
    private KycAuditLogRepository kycAuditLogRepository;

    @Mock
    private UidaiService uidaiService;

    @Mock
    private PanVerificationService panVerificationService;

    @Mock
    private KycEncryptionUtil encryptionUtil;

    @InjectMocks
    private KycService kycService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kycService, "limitLevel0", new BigDecimal("10000.00"));
        ReflectionTestUtils.setField(kycService, "limitLevel1", new BigDecimal("100000.00"));
        ReflectionTestUtils.setField(kycService, "limitLevel2", new BigDecimal("999999999.00"));
        ReflectionTestUtils.setField(kycService, "otpExpirySeconds", 300);
    }

    @Test
    void testInitiateAadhaarOtp_NewUser() {
        AadhaarOtpRequest request = new AadhaarOtpRequest("user123", "user123@upimesh", "123456789012");
        when(kycRecordRepository.findByUserId("user123")).thenReturn(Optional.empty());
        when(uidaiService.generateOtp(anyString(), eq("123456789012"))).thenReturn("654321");

        AadhaarOtpResponse response = kycService.initiateAadhaarOtp(request, "127.0.0.1", "device123");

        assertNotNull(response);
        assertNotNull(response.getKycId());
        assertTrue(response.getMessage().contains("654321"));
        verify(kycRecordRepository, times(1)).save(any(KycRecord.class));
    }

    @Test
    void testVerifyAadhaarOtp_Success() {
        KycRecord record = KycRecord.builder()
                .kycId("kyc-uuid")
                .userId("user123")
                .userUpiId("user123@upimesh")
                .kycLevel(KycLevel.LEVEL_0)
                .status(KycStatus.IN_PROGRESS)
                .aadhaarVerified(false)
                .build();

        UidaiService.UidaiEkycData ekycData = UidaiService.UidaiEkycData.builder()
                .fullName("Rohan Sharma")
                .dateOfBirth(LocalDate.of(1995, 8, 15))
                .address("Mumbai")
                .maskedAadhaar("XXXX-XXXX-9012")
                .build();

        AadhaarVerifyRequest verifyRequest = new AadhaarVerifyRequest("kyc-uuid", "654321");

        when(kycRecordRepository.findByKycId("kyc-uuid")).thenReturn(Optional.of(record));
        when(uidaiService.verifyOtpAndFetchDetails("kyc-uuid", "654321")).thenReturn(ekycData);
        when(encryptionUtil.encrypt(anyString())).thenReturn("encrypted-ref");

        KycStatusResponse response = kycService.verifyAadhaarOtp(verifyRequest, "127.0.0.1", "device123");

        assertNotNull(response);
        assertTrue(response.isAadhaarVerified());
        assertEquals("Rohan Sharma", response.getFullName());
        assertEquals(KycLevel.LEVEL_0, response.getKycLevel());
        verify(kycRecordRepository, times(1)).save(record);
        verify(kycDocumentRepository, times(1)).save(any());
    }

    @Test
    void testVerifyAadhaarOtp_WrongOtp() {
        KycRecord record = KycRecord.builder()
                .kycId("kyc-uuid")
                .userId("user123")
                .aadhaarVerified(false)
                .build();

        AadhaarVerifyRequest verifyRequest = new AadhaarVerifyRequest("kyc-uuid", "000000");

        when(kycRecordRepository.findByKycId("kyc-uuid")).thenReturn(Optional.of(record));
        when(uidaiService.verifyOtpAndFetchDetails("kyc-uuid", "000000"))
                .thenThrow(new IllegalArgumentException("Incorrect OTP code entered"));

        assertThrows(IllegalArgumentException.class, () -> {
            kycService.verifyAadhaarOtp(verifyRequest, "127.0.0.1", "device123");
        });
    }

    @Test
    void testVerifyPan_Success_UpgradesToLevel1() {
        KycRecord record = KycRecord.builder()
                .kycId("kyc-uuid")
                .userId("user123")
                .aadhaarVerified(true)
                .panVerified(false)
                .build();

        PanVerifyRequest panRequest = new PanVerifyRequest("kyc-uuid", "ABCDE1234F", "Rohan Sharma", LocalDate.of(1995, 8, 15));
        PanVerificationService.PanVerificationResult panResult = PanVerificationService.PanVerificationResult.builder()
                .valid(true)
                .message("PAN verified")
                .nameOnCard("ROHAN SHARMA")
                .build();

        when(kycRecordRepository.findByKycId("kyc-uuid")).thenReturn(Optional.of(record));
        when(panVerificationService.verifyPan("ABCDE1234F", "Rohan Sharma", LocalDate.of(1995, 8, 15))).thenReturn(panResult);
        when(encryptionUtil.encrypt("ABCDE1234F")).thenReturn("encrypted-pan");
        when(encryptionUtil.decrypt("encrypted-pan")).thenReturn("ABCDE1234F");

        KycStatusResponse response = kycService.verifyPan(panRequest, "127.0.0.1", "device123");

        assertNotNull(response);
        assertTrue(response.isPanVerified());
        assertEquals(KycLevel.LEVEL_1, response.getKycLevel());
        assertEquals(new BigDecimal("100000.00"), response.getMonthlyLimit());
        verify(kycRecordRepository, times(1)).save(record);
    }

    @Test
    void testVerifyPan_Fails_RejectPanBeforeAadhaar() {
        KycRecord record = KycRecord.builder()
                .kycId("kyc-uuid")
                .userId("user123")
                .aadhaarVerified(false)
                .panVerified(false)
                .build();

        PanVerifyRequest panRequest = new PanVerifyRequest("kyc-uuid", "ABCDE1234F", "Rohan Sharma", LocalDate.of(1995, 8, 15));

        when(kycRecordRepository.findByKycId("kyc-uuid")).thenReturn(Optional.of(record));

        assertThrows(InvalidKycStateException.class, () -> {
            kycService.verifyPan(panRequest, "127.0.0.1", "device123");
        });
    }

    @Test
    void testFaceMatch_Success_UpgradesToLevel2() {
        KycRecord record = KycRecord.builder()
                .kycId("kyc-uuid")
                .userId("user123")
                .aadhaarVerified(true)
                .panVerified(true)
                .faceMatched(false)
                .build();

        when(kycRecordRepository.findByKycId("kyc-uuid")).thenReturn(Optional.of(record));
        when(encryptionUtil.encrypt(anyString())).thenReturn("encrypted-selfie");

        KycStatusResponse response = kycService.performFaceMatch("kyc-uuid", "selfieBase64DataString", "127.0.0.1", "device123");

        assertNotNull(response);
        assertTrue(response.isFaceMatched());
        assertEquals(KycLevel.LEVEL_2, response.getKycLevel());
        assertEquals(new BigDecimal("999999999.00"), response.getMonthlyLimit());
        assertEquals(KycStatus.COMPLETED, response.getStatus());
        verify(kycRecordRepository, times(1)).save(record);
    }
}
