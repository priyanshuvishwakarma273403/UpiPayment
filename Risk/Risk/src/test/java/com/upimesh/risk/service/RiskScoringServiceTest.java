package com.upimesh.risk.service;

import com.upimesh.risk.model.dto.AmountResult;
import com.upimesh.risk.model.dto.DeviceResult;
import com.upimesh.risk.model.dto.LocationResult;
import com.upimesh.risk.model.dto.TimeResult;
import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.model.entity.RiskScoringResult;
import com.upimesh.risk.model.enums.RiskLevel;
import com.upimesh.risk.model.request.RiskScoringRequest;
import com.upimesh.risk.model.response.RiskScoringResponse;
import com.upimesh.risk.repository.RiskProfileRepository;
import com.upimesh.risk.repository.RiskScoringResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RiskScoringServiceTest {

    @Mock
    private RiskProfileRepository profileRepository;

    @Mock
    private RiskScoringResultRepository scoringResultRepository;

    @Mock
    private DeviceFingerprintService deviceFingerprintService;

    @Mock
    private LocationAnalysisService locationAnalysisService;

    @Mock
    private BehavioralAnalysisService behavioralAnalysisService;

    @Mock
    private RiskProfileUpdater riskProfileUpdater;

    @InjectMocks
    private RiskScoringService riskScoringService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(riskScoringService, "lowThreshold", 0.3);
        ReflectionTestUtils.setField(riskScoringService, "highThreshold", 0.7);
    }

    @Test
    void testLowRiskPassesThrough() {
        RiskScoringRequest request = new RiskScoringRequest("tx123", "user123", "sender@upimesh", "receiver@upimesh",
                new BigDecimal("5000.00"), "dev123", "127.0.0.1", "Mumbai", "PAYMENT");

        RiskProfile profile = RiskProfile.builder()
                .userId("user123")
                .baseRiskScore(0.1)
                .build();

        when(profileRepository.findByUserId("user123")).thenReturn(Optional.of(profile));
        when(deviceFingerprintService.analyzeDevice(eq("dev123"), eq("user123")))
                .thenReturn(new DeviceResult(false, 0.0));
        when(locationAnalysisService.analyzeLocation(eq("Mumbai"), eq("user123")))
                .thenReturn(new LocationResult(false, 0.0, "OK"));
        when(behavioralAnalysisService.analyzeTimePattern(anyInt(), eq("user123")))
                .thenReturn(new TimeResult(false, 0.0));
        when(behavioralAnalysisService.analyzeAmountPattern(any(BigDecimal.class), eq("user123")))
                .thenReturn(new AmountResult(false, 0.0));
        when(scoringResultRepository.findByUserIdOrderByScoredAtDesc("user123"))
                .thenReturn(Collections.emptyList());

        RiskScoringResponse response = riskScoringService.scoreTransaction(request);

        assertNotNull(response);
        assertTrue(response.isAllowed());
        assertEquals(RiskLevel.LOW, response.getRiskLevel());
        assertEquals(0.1, response.getFinalScore());
        verify(scoringResultRepository, times(1)).save(any(RiskScoringResult.class));
        verify(riskProfileUpdater, times(1)).updateRiskProfile(eq("user123"), eq(request), eq(true));
    }

    @Test
    void testNewDeviceBoostTriggered() {
        RiskScoringRequest request = new RiskScoringRequest("tx123", "user123", "sender@upimesh", "receiver@upimesh",
                new BigDecimal("5000.00"), "newDev123", "127.0.0.1", "Mumbai", "PAYMENT");

        RiskProfile profile = RiskProfile.builder()
                .userId("user123")
                .baseRiskScore(0.1)
                .build();

        when(profileRepository.findByUserId("user123")).thenReturn(Optional.of(profile));
        when(deviceFingerprintService.analyzeDevice(eq("newDev123"), eq("user123")))
                .thenReturn(new DeviceResult(true, 0.2));
        when(locationAnalysisService.analyzeLocation(eq("Mumbai"), eq("user123")))
                .thenReturn(new LocationResult(false, 0.0, "OK"));
        when(behavioralAnalysisService.analyzeTimePattern(anyInt(), eq("user123")))
                .thenReturn(new TimeResult(false, 0.0));
        when(behavioralAnalysisService.analyzeAmountPattern(any(BigDecimal.class), eq("user123")))
                .thenReturn(new AmountResult(false, 0.0));
        when(scoringResultRepository.findByUserIdOrderByScoredAtDesc("user123"))
                .thenReturn(Collections.emptyList());

        RiskScoringResponse response = riskScoringService.scoreTransaction(request);

        assertNotNull(response);
        assertTrue(response.isAllowed()); // 0.1 + 0.2 = 0.3 (LOW since threshold is 0.3)
        assertEquals(0.3, response.getFinalScore(), 1e-4);
        assertTrue(response.getFactorsTriggered().contains("NEW_DEVICE"));
    }

    @Test
    void testLocationAnomalyDetected() {
        RiskScoringRequest request = new RiskScoringRequest("tx123", "user123", "sender@upimesh", "receiver@upimesh",
                new BigDecimal("5000.00"), "dev123", "127.0.0.1", "Delhi", "PAYMENT");

        RiskProfile profile = RiskProfile.builder()
                .userId("user123")
                .baseRiskScore(0.1)
                .build();

        when(profileRepository.findByUserId("user123")).thenReturn(Optional.of(profile));
        when(deviceFingerprintService.analyzeDevice(eq("dev123"), eq("user123")))
                .thenReturn(new DeviceResult(false, 0.0));
        when(locationAnalysisService.analyzeLocation(eq("Delhi"), eq("user123")))
                .thenReturn(new LocationResult(true, 0.25, "Anomaly"));
        when(behavioralAnalysisService.analyzeTimePattern(anyInt(), eq("user123")))
                .thenReturn(new TimeResult(false, 0.0));
        when(behavioralAnalysisService.analyzeAmountPattern(any(BigDecimal.class), eq("user123")))
                .thenReturn(new AmountResult(false, 0.0));
        when(scoringResultRepository.findByUserIdOrderByScoredAtDesc("user123"))
                .thenReturn(Collections.emptyList());

        RiskScoringResponse response = riskScoringService.scoreTransaction(request);

        assertNotNull(response);
        assertTrue(response.isAllowed()); // 0.1 + 0.25 = 0.35 (MEDIUM)
        assertEquals(RiskLevel.MEDIUM, response.getRiskLevel());
        assertEquals(0.35, response.getFinalScore(), 1e-4);
        assertTrue(response.getFactorsTriggered().contains("LOCATION_ANOMALY"));
    }

    @Test
    void testUnusualHoursBoostApplied() {
        RiskScoringRequest request = new RiskScoringRequest("tx123", "user123", "sender@upimesh", "receiver@upimesh",
                new BigDecimal("5000.00"), "dev123", "127.0.0.1", "Mumbai", "PAYMENT");

        RiskProfile profile = RiskProfile.builder()
                .userId("user123")
                .baseRiskScore(0.1)
                .build();

        when(profileRepository.findByUserId("user123")).thenReturn(Optional.of(profile));
        when(deviceFingerprintService.analyzeDevice(eq("dev123"), eq("user123")))
                .thenReturn(new DeviceResult(false, 0.0));
        when(locationAnalysisService.analyzeLocation(eq("Mumbai"), eq("user123")))
                .thenReturn(new LocationResult(false, 0.0, "OK"));
        when(behavioralAnalysisService.analyzeTimePattern(anyInt(), eq("user123")))
                .thenReturn(new TimeResult(true, 0.1));
        when(behavioralAnalysisService.analyzeAmountPattern(any(BigDecimal.class), eq("user123")))
                .thenReturn(new AmountResult(false, 0.0));
        when(scoringResultRepository.findByUserIdOrderByScoredAtDesc("user123"))
                .thenReturn(Collections.emptyList());

        RiskScoringResponse response = riskScoringService.scoreTransaction(request);

        assertNotNull(response);
        assertTrue(response.isAllowed()); // 0.1 + 0.1 = 0.2 (LOW)
        assertEquals(0.2, response.getFinalScore());
        assertTrue(response.getFactorsTriggered().contains("UNUSUAL_HOUR"));
    }

    @Test
    void testLargeAmountUnusualPattern() {
        RiskScoringRequest request = new RiskScoringRequest("tx123", "user123", "sender@upimesh", "receiver@upimesh",
                new BigDecimal("55000.00"), "dev123", "127.0.0.1", "Mumbai", "PAYMENT");

        RiskProfile profile = RiskProfile.builder()
                .userId("user123")
                .baseRiskScore(0.1)
                .build();

        when(profileRepository.findByUserId("user123")).thenReturn(Optional.of(profile));
        when(deviceFingerprintService.analyzeDevice(eq("dev123"), eq("user123")))
                .thenReturn(new DeviceResult(false, 0.0));
        when(locationAnalysisService.analyzeLocation(eq("Mumbai"), eq("user123")))
                .thenReturn(new LocationResult(false, 0.0, "OK"));
        when(behavioralAnalysisService.analyzeTimePattern(anyInt(), eq("user123")))
                .thenReturn(new TimeResult(false, 0.0));
        when(behavioralAnalysisService.analyzeAmountPattern(any(BigDecimal.class), eq("user123")))
                .thenReturn(new AmountResult(true, 0.2));
        when(scoringResultRepository.findByUserIdOrderByScoredAtDesc("user123"))
                .thenReturn(Collections.emptyList());

        RiskScoringResponse response = riskScoringService.scoreTransaction(request);

        assertNotNull(response);
        assertTrue(response.isAllowed()); // 0.1 + 0.2 (unusual amount) + 0.1 (large amount > 50k) = 0.4 (MEDIUM)
        assertEquals(0.4, response.getFinalScore());
        assertTrue(response.getFactorsTriggered().contains("VELOCITY_HIGH"));
        assertTrue(response.getFactorsTriggered().contains("LARGE_AMOUNT"));
    }

    @Test
    void testHighRiskBlocked() {
        RiskScoringRequest request = new RiskScoringRequest("tx123", "user123", "sender@upimesh", "receiver@upimesh",
                new BigDecimal("55000.00"), "newDev123", "127.0.0.1", "Delhi", "PAYMENT");

        RiskProfile profile = RiskProfile.builder()
                .userId("user123")
                .baseRiskScore(0.1)
                .build();

        when(profileRepository.findByUserId("user123")).thenReturn(Optional.of(profile));
        // new device (+0.2)
        when(deviceFingerprintService.analyzeDevice(eq("newDev123"), eq("user123")))
                .thenReturn(new DeviceResult(true, 0.2));
        // location anomaly (+0.25)
        when(locationAnalysisService.analyzeLocation(eq("Delhi"), eq("user123")))
                .thenReturn(new LocationResult(true, 0.25, "Anomaly"));
        // unusual timing (+0.05)
        when(behavioralAnalysisService.analyzeTimePattern(anyInt(), eq("user123")))
                .thenReturn(new TimeResult(true, 0.05));
        // amount anomaly (+0.2)
        when(behavioralAnalysisService.analyzeAmountPattern(any(BigDecimal.class), eq("user123")))
                .thenReturn(new AmountResult(true, 0.2));
        when(scoringResultRepository.findByUserIdOrderByScoredAtDesc("user123"))
                .thenReturn(Collections.emptyList());

        // Total score = 0.1 + 0.2 + 0.25 + 0.05 + 0.2 + 0.1 (large amount > 50k) = 0.9. Capped at 1.0.

        RiskScoringResponse response = riskScoringService.scoreTransaction(request);

        assertNotNull(response);
        assertFalse(response.isAllowed()); // finalScore >= 0.7
        assertEquals(RiskLevel.CRITICAL, response.getRiskLevel());
        assertEquals(0.9, response.getFinalScore());
        verify(riskProfileUpdater, times(1)).updateRiskProfile(eq("user123"), eq(request), eq(false));
    }
}
