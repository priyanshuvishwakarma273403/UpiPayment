package com.upimesh.aml.service;

import com.upimesh.aml.model.dto.StructuringResult;
import com.upimesh.aml.model.dto.VelocityResult;
import com.upimesh.aml.model.dto.WatchlistResult;
import com.upimesh.aml.model.entity.AmlAlert;
import com.upimesh.aml.model.entity.AmlScreeningResult;
import com.upimesh.aml.model.enums.AmlRiskLevel;
import com.upimesh.aml.model.request.AmlCheckRequest;
import com.upimesh.aml.model.response.AmlCheckResponse;
import com.upimesh.aml.repository.AmlAlertRepository;
import com.upimesh.aml.repository.AmlScreeningResultRepository;
import com.upimesh.aml.repository.WatchlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AmlServiceTest {

    @Mock
    private AmlScreeningResultRepository screeningRepository;

    @Mock
    private AmlAlertRepository alertRepository;

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private VelocityCheckService velocityCheckService;

    @Mock
    private StructuringDetectionService structuringDetectionService;

    @Mock
    private WatchlistScreeningService watchlistScreeningService;

    @InjectMocks
    private AmlService amlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(amlService, "highValueThreshold", new BigDecimal("50000"));
    }

    @Test
    void testLowRiskPassesThrough() {
        AmlCheckRequest request = new AmlCheckRequest("tx123", "sender@upimesh", "receiver@upimesh", 
                new BigDecimal("5000.00"), "PAYMENT", "dev123", "127.0.0.1");

        when(velocityCheckService.checkVelocity(anyString(), any(BigDecimal.class)))
                .thenReturn(new VelocityResult(false, "OK", 1, 1));
        when(structuringDetectionService.detectStructuring(anyString(), any(BigDecimal.class)))
                .thenReturn(new StructuringResult(false, new BigDecimal("5000.00"), 1, "OK"));
        when(watchlistScreeningService.screenName("sender")).thenReturn(new WatchlistResult(false, null, null, null));
        when(watchlistScreeningService.screenName("receiver")).thenReturn(new WatchlistResult(false, null, null, null));

        AmlCheckResponse response = amlService.screenTransaction(request);

        assertNotNull(response);
        assertTrue(response.isAllowed());
        assertEquals(AmlRiskLevel.LOW, response.getRiskLevel());
        assertEquals(0.0, response.getRiskScore());
        verify(screeningRepository, times(1)).save(any(AmlScreeningResult.class));
        verify(alertRepository, never()).save(any(AmlAlert.class));
    }

    @Test
    void testVelocityBreachFlagged() {
        AmlCheckRequest request = new AmlCheckRequest("tx123", "sender@upimesh", "receiver@upimesh", 
                new BigDecimal("5000.00"), "PAYMENT", "dev123", "127.0.0.1");

        when(velocityCheckService.checkVelocity(anyString(), any(BigDecimal.class)))
                .thenReturn(new VelocityResult(true, "Exceeded limit", 11, 11));
        when(structuringDetectionService.detectStructuring(anyString(), any(BigDecimal.class)))
                .thenReturn(new StructuringResult(false, new BigDecimal("5000.00"), 1, "OK"));
        when(watchlistScreeningService.screenName("sender")).thenReturn(new WatchlistResult(false, null, null, null));
        when(watchlistScreeningService.screenName("receiver")).thenReturn(new WatchlistResult(false, null, null, null));

        AmlCheckResponse response = amlService.screenTransaction(request);

        assertNotNull(response);
        assertTrue(response.isAllowed()); 
        assertEquals(AmlRiskLevel.MEDIUM, response.getRiskLevel());
        assertEquals(0.3, response.getRiskScore());
        verify(screeningRepository, times(1)).save(any(AmlScreeningResult.class));
        verify(alertRepository, times(1)).save(any(AmlAlert.class));
    }

    @Test
    void testStructuringDetected() {
        AmlCheckRequest request = new AmlCheckRequest("tx123", "sender@upimesh", "receiver@upimesh", 
                new BigDecimal("8000.00"), "PAYMENT", "dev123", "127.0.0.1");

        when(velocityCheckService.checkVelocity(anyString(), any(BigDecimal.class)))
                .thenReturn(new VelocityResult(false, "OK", 1, 1));
        when(structuringDetectionService.detectStructuring(anyString(), any(BigDecimal.class)))
                .thenReturn(new StructuringResult(true, new BigDecimal("95000.00"), 10, "Breached"));
        when(watchlistScreeningService.screenName("sender")).thenReturn(new WatchlistResult(false, null, null, null));
        when(watchlistScreeningService.screenName("receiver")).thenReturn(new WatchlistResult(false, null, null, null));

        AmlCheckResponse response = amlService.screenTransaction(request);

        assertNotNull(response);
        assertTrue(response.isAllowed()); 
        assertEquals(AmlRiskLevel.MEDIUM, response.getRiskLevel());
        assertEquals(0.5, response.getRiskScore());
        verify(screeningRepository, times(1)).save(any(AmlScreeningResult.class));
        verify(alertRepository, times(1)).save(any(AmlAlert.class));
    }

    @Test
    void testWatchlistMatchBlocked() {
        AmlCheckRequest request = new AmlCheckRequest("tx123", "terrorist@upimesh", "receiver@upimesh", 
                new BigDecimal("5000.00"), "PAYMENT", "dev123", "127.0.0.1");

        when(velocityCheckService.checkVelocity(anyString(), any(BigDecimal.class)))
                .thenReturn(new VelocityResult(false, "OK", 1, 1));
        when(structuringDetectionService.detectStructuring(anyString(), any(BigDecimal.class)))
                .thenReturn(new StructuringResult(false, new BigDecimal("5000.00"), 1, "OK"));
        when(watchlistScreeningService.screenName("terrorist"))
                .thenReturn(new WatchlistResult(true, "Terrorist Name", "SANCTIONS", "OFAC"));
        when(watchlistScreeningService.screenName("receiver")).thenReturn(new WatchlistResult(false, null, null, null));

        AmlCheckResponse response = amlService.screenTransaction(request);

        assertNotNull(response);
        assertFalse(response.isAllowed()); 
        assertEquals(AmlRiskLevel.BLOCKED, response.getRiskLevel());
        assertEquals(0.8, response.getRiskScore());
        assertNotNull(response.getBlockReason());
        verify(screeningRepository, times(1)).save(any(AmlScreeningResult.class));
        verify(alertRepository, times(1)).save(any(AmlAlert.class));
    }

    @Test
    void testHighValueFlaggedNotBlocked() {
        AmlCheckRequest request = new AmlCheckRequest("tx123", "sender@upimesh", "receiver@upimesh", 
                new BigDecimal("55000.00"), "PAYMENT", "dev123", "127.0.0.1");

        when(velocityCheckService.checkVelocity(anyString(), any(BigDecimal.class)))
                .thenReturn(new VelocityResult(false, "OK", 1, 1));
        when(structuringDetectionService.detectStructuring(anyString(), any(BigDecimal.class)))
                .thenReturn(new StructuringResult(false, new BigDecimal("55000.00"), 1, "OK"));
        when(watchlistScreeningService.screenName("sender")).thenReturn(new WatchlistResult(false, null, null, null));
        when(watchlistScreeningService.screenName("receiver")).thenReturn(new WatchlistResult(false, null, null, null));

        AmlCheckResponse response = amlService.screenTransaction(request);

        assertNotNull(response);
        assertTrue(response.isAllowed()); 
        assertEquals(AmlRiskLevel.LOW, response.getRiskLevel());
        assertEquals(0.2, response.getRiskScore());
        verify(screeningRepository, times(1)).save(any(AmlScreeningResult.class));
        verify(alertRepository, never()).save(any(AmlAlert.class)); 
    }

    @Test
    void testCompositeScoreCalculation() {
        AmlCheckRequest request = new AmlCheckRequest("tx123", "sender@upimesh", "receiver@upimesh", 
                new BigDecimal("55000.00"), "PAYMENT", "dev123", "127.0.0.1");

        when(velocityCheckService.checkVelocity(anyString(), any(BigDecimal.class)))
                .thenReturn(new VelocityResult(true, "Exceeded limit", 11, 11));
        when(structuringDetectionService.detectStructuring(anyString(), any(BigDecimal.class)))
                .thenReturn(new StructuringResult(true, new BigDecimal("95000.00"), 3, "Pattern"));
        when(watchlistScreeningService.screenName("sender"))
                .thenReturn(new WatchlistResult(true, "Target name", "PEP", "UN"));
        when(watchlistScreeningService.screenName("receiver")).thenReturn(new WatchlistResult(false, null, null, null));

        AmlCheckResponse response = amlService.screenTransaction(request);

        assertNotNull(response);
        assertFalse(response.isAllowed());
        assertEquals(AmlRiskLevel.BLOCKED, response.getRiskLevel());
        assertEquals(1.0, response.getRiskScore());
    }
}
