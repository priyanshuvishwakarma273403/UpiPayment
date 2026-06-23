package com.upimesh.referral.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upimesh.referral.feign.RewardsServiceClient;
import com.upimesh.referral.feign.dto.ApplyReferralRequest;
import com.upimesh.referral.feign.dto.RewardsApiResponse;
import com.upimesh.referral.model.entity.ReferralCode;
import com.upimesh.referral.model.entity.ReferralRecord;
import com.upimesh.referral.model.enums.ReferralRewardStatus;
import com.upimesh.referral.model.enums.ReferralStatus;
import com.upimesh.referral.model.response.LeaderboardEntry;
import com.upimesh.referral.model.response.ReferralStatsResponse;
import com.upimesh.referral.repository.ReferralCodeRepository;
import com.upimesh.referral.repository.ReferralRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReferralServiceTest {

    @Mock
    private ReferralCodeRepository codeRepository;

    @Mock
    private ReferralRecordRepository recordRepository;

    @Mock
    private RewardsServiceClient rewardsServiceClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ReferralService referralService;

    private String referrerId;
    private String refereeId;
    private String referralCodeStr;

    @BeforeEach
    public void setUp() {
        referrerId = "referrer123";
        refereeId = "referee789";
        referralCodeStr = "REFCODE123";
        org.springframework.test.util.ReflectionTestUtils.setField(referralService, "internalKey", "test-key");
    }

    // ─── 1. REFERRAL CODE GENERATION TESTS ───────────────────────────────────

    @Test
    public void testGenerateReferralCode_ReturnsExisting() {
        ReferralCode code = ReferralCode.builder()
                .userId(referrerId)
                .code(referralCodeStr)
                .build();
        when(codeRepository.findByUserId(referrerId)).thenReturn(Optional.of(code));

        ReferralCode result = referralService.generateReferralCode(referrerId);

        assertNotNull(result);
        assertEquals(referralCodeStr, result.getCode());
        verify(codeRepository, never()).save(any());
    }

    @Test
    public void testGenerateReferralCode_CreatesNew() {
        when(codeRepository.findByUserId(referrerId)).thenReturn(Optional.empty());
        when(codeRepository.existsByCode(anyString())).thenReturn(false);
        when(codeRepository.save(any(ReferralCode.class))).thenAnswer(i -> i.getArgument(0));

        ReferralCode result = referralService.generateReferralCode(referrerId);

        assertNotNull(result);
        assertEquals(referrerId, result.getUserId());
        assertEquals(10, result.getCode().length());
        verify(codeRepository, times(1)).save(any());
    }

    // ─── 2. TRACK REFERRAL & ANTI-FRAUD TESTS ────────────────────────────────

    @Test
    public void testTrackReferral_Success() {
        ReferralCode referralCode = ReferralCode.builder()
                .userId(referrerId)
                .code(referralCodeStr)
                .userUpiId("referrer123@upimesh")
                .isActive(true)
                .totalReferrals(0)
                .build();

        when(codeRepository.findByCode(referralCodeStr)).thenReturn(Optional.of(referralCode));
        when(recordRepository.existsByReferrerIdAndDeviceId(referrerId, "deviceXYZ")).thenReturn(false);
        when(recordRepository.existsByRefereeId(refereeId)).thenReturn(false);
        when(recordRepository.countByIpAddress("192.168.1.1")).thenReturn(0L);
        when(recordRepository.save(any(ReferralRecord.class))).thenAnswer(i -> i.getArgument(0));

        ReferralRecord record = referralService.trackReferral(referralCodeStr, refereeId, "referee789@upimesh", "deviceXYZ", "192.168.1.1");

        assertNotNull(record);
        assertEquals(ReferralStatus.PENDING, record.getStatus());
        assertEquals(referrerId, record.getReferrerId());
        assertEquals(refereeId, record.getRefereeId());
        assertEquals(BigDecimal.valueOf(50.00), record.getReferrerRewardAmount());
        assertEquals(BigDecimal.valueOf(25.00), record.getRefereeRewardAmount());

        verify(codeRepository).save(referralCode);
        assertEquals(1, referralCode.getTotalReferrals());
    }

    @Test
    public void testTrackReferral_SelfReferralBlocked() {
        ReferralCode referralCode = ReferralCode.builder()
                .userId(referrerId)
                .code(referralCodeStr)
                .isActive(true)
                .build();
        when(codeRepository.findByCode(referralCodeStr)).thenReturn(Optional.of(referralCode));

        assertThrows(IllegalArgumentException.class, () -> {
            // Referee is same as Referrer
            referralService.trackReferral(referralCodeStr, referrerId, "ref@upi", "dev", "ip");
        });
    }

    @Test
    public void testTrackReferral_DeviceFraudBlocked() {
        ReferralCode referralCode = ReferralCode.builder()
                .userId(referrerId)
                .code(referralCodeStr)
                .isActive(true)
                .build();
        when(codeRepository.findByCode(referralCodeStr)).thenReturn(Optional.of(referralCode));
        // Referee uses a device ID already used by this referrer
        when(recordRepository.existsByReferrerIdAndDeviceId(referrerId, "deviceXYZ")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            referralService.trackReferral(referralCodeStr, refereeId, "referee@upi", "deviceXYZ", "ip");
        });
    }

    @Test
    public void testTrackReferral_DuplicateRefereeBlocked() {
        ReferralCode referralCode = ReferralCode.builder()
                .userId(referrerId)
                .code(referralCodeStr)
                .isActive(true)
                .build();
        when(codeRepository.findByCode(referralCodeStr)).thenReturn(Optional.of(referralCode));
        when(recordRepository.existsByReferrerIdAndDeviceId(referrerId, "deviceXYZ")).thenReturn(false);
        // Referee has already been referred before
        when(recordRepository.existsByRefereeId(refereeId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            referralService.trackReferral(referralCodeStr, refereeId, "referee@upi", "deviceXYZ", "ip");
        });
    }

    @Test
    public void testTrackReferral_IpAddressFraudBlocked() {
        ReferralCode referralCode = ReferralCode.builder()
                .userId(referrerId)
                .code(referralCodeStr)
                .isActive(true)
                .build();
        when(codeRepository.findByCode(referralCodeStr)).thenReturn(Optional.of(referralCode));
        when(recordRepository.existsByReferrerIdAndDeviceId(referrerId, "deviceXYZ")).thenReturn(false);
        when(recordRepository.existsByRefereeId(refereeId)).thenReturn(false);
        // This IP already registered 3 referrals
        when(recordRepository.countByIpAddress("192.168.1.1")).thenReturn(3L);

        assertThrows(IllegalArgumentException.class, () -> {
            referralService.trackReferral(referralCodeStr, refereeId, "referee@upi", "deviceXYZ", "192.168.1.1");
        });
    }

    // ─── 3. QUALIFICATION TESTS ──────────────────────────────────────────────

    @Test
    public void testQualifyReferral_UnderThreshold() {
        ReferralRecord record = ReferralRecord.builder()
                .referralId("REF123")
                .refereeId(refereeId)
                .status(ReferralStatus.PENDING)
                .build();
        when(recordRepository.findByRefereeIdAndStatus(refereeId, ReferralStatus.PENDING))
                .thenReturn(Optional.of(record));

        // Amount ₹99.99 is less than ₹100 threshold
        referralService.qualifyReferral(refereeId, "TXN001", BigDecimal.valueOf(99.99));

        assertEquals(ReferralStatus.PENDING, record.getStatus());
        verify(recordRepository, never()).save(any());
    }

    @Test
    public void testQualifyReferral_Qualifies() {
        ReferralRecord record = ReferralRecord.builder()
                .referralId("REF123")
                .referralCode(referralCodeStr)
                .refereeId(refereeId)
                .status(ReferralStatus.PENDING)
                .build();
        ReferralCode code = ReferralCode.builder()
                .code(referralCodeStr)
                .qualifiedReferrals(0)
                .build();

        when(recordRepository.findByRefereeIdAndStatus(refereeId, ReferralStatus.PENDING))
                .thenReturn(Optional.of(record));
        when(codeRepository.findByCode(referralCodeStr)).thenReturn(Optional.of(code));

        // Amount ₹150 meets threshold
        referralService.qualifyReferral(refereeId, "TXN001", BigDecimal.valueOf(150.00));

        assertEquals(ReferralStatus.QUALIFIED, record.getStatus());
        assertEquals("TXN001", record.getRefereeFirstTransactionId());
        assertNotNull(record.getQualifiedAt());
        assertNotNull(record.getRewardScheduledAt());

        verify(recordRepository).save(record);
        verify(codeRepository).save(code);
        assertEquals(1, code.getQualifiedReferrals());
    }

    // ─── 4. REWARD CREDIT TESTS ──────────────────────────────────────────────

    @Test
    public void testCreditReferralRewards_Success() {
        ReferralRecord record = ReferralRecord.builder()
                .referralId("REF123")
                .referralCode(referralCodeStr)
                .refereeId(refereeId)
                .referrerRewardAmount(BigDecimal.valueOf(50.00))
                .status(ReferralStatus.QUALIFIED)
                .build();
        ReferralCode code = ReferralCode.builder()
                .code(referralCodeStr)
                .totalEarned(BigDecimal.ZERO)
                .build();

        when(recordRepository.findByReferralId("REF123")).thenReturn(Optional.of(record));
        when(codeRepository.findByCode(referralCodeStr)).thenReturn(Optional.of(code));
        when(rewardsServiceClient.applyReferral(eq("test-key"), any(ApplyReferralRequest.class)))
                .thenReturn(RewardsApiResponse.<Void>builder().success(true).build());

        referralService.creditReferralRewards("REF123");

        assertEquals(ReferralStatus.REWARDED, record.getStatus());
        assertEquals(ReferralRewardStatus.PAID, record.getReferrerRewardStatus());
        assertEquals(ReferralRewardStatus.PAID, record.getRefereeRewardStatus());
        assertEquals(BigDecimal.valueOf(50.00), code.getTotalEarned());

        verify(recordRepository).save(record);
        verify(codeRepository).save(code);
    }

    // ─── 5. LEADERBOARD AND REDIS CACHE TESTS ────────────────────────────────

    @Test
    public void testGetLeaderboard_CacheHit() throws Exception {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        leaderboard.add(LeaderboardEntry.builder()
                .userId(referrerId)
                .totalReferrals(5)
                .rank(1)
                .build());

        String json = objectMapper.writeValueAsString(leaderboard);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("referrals:leaderboard")).thenReturn(json);

        List<LeaderboardEntry> result = referralService.getLeaderboard();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(referrerId, result.get(0).getUserId());
        // Verify database is never queried
        verify(codeRepository, never()).findTop10ByOrderByTotalReferralsDesc();
    }

    @Test
    public void testGetLeaderboard_CacheMiss() {
        ReferralCode code1 = ReferralCode.builder()
                .userId(referrerId)
                .userUpiId("referrer123@upimesh")
                .code(referralCodeStr)
                .totalReferrals(5)
                .qualifiedReferrals(3)
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("referrals:leaderboard")).thenReturn(null);
        when(codeRepository.findTop10ByOrderByTotalReferralsDesc()).thenReturn(Arrays.asList(code1));

        List<LeaderboardEntry> result = referralService.getLeaderboard();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(referrerId, result.get(0).getUserId());
        assertEquals(1, result.get(0).getRank());

        // Verify it was cached
        verify(valueOperations).set(eq("referrals:leaderboard"), anyString(), any(Duration.class));
    }
}
