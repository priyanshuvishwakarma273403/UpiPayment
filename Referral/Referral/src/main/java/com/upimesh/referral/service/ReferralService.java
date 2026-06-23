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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralService {

    private final ReferralCodeRepository codeRepository;
    private final ReferralRecordRepository recordRepository;
    private final RewardsServiceClient rewardsServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${referral.internal-key:internal-secret-change-in-prod}")
    private String internalKey;

    @Value("${referral.reward-delay-ms:86400000}") // Default 24 hours in milliseconds
    private long rewardDelayMs;

    private static final String LEADERBOARD_CACHE_KEY = "referrals:leaderboard";

    /**
     * Generates a unique 10-character alphanumeric referral code for a user.
     */
    @Transactional
    public ReferralCode generateReferralCode(String userId) {
        log.info("Generating referral code for user: {}", userId);

        return codeRepository.findByUserId(userId)
                .orElseGet(() -> {
                    String code;
                    do {
                        code = UUID.randomUUID().toString().replace("-", "")
                                .substring(0, 10).toUpperCase();
                    } while (codeRepository.existsByCode(code));

                    ReferralCode newCode = ReferralCode.builder()
                            .codeId("RCD" + UUID.randomUUID().toString().replace("-", "").substring(0, 15).toUpperCase())
                            .userId(userId)
                            .userUpiId(userId + "@upimesh")
                            .code(code)
                            .totalReferrals(0)
                            .qualifiedReferrals(0)
                            .totalEarned(BigDecimal.ZERO)
                            .isActive(true)
                            .build();

                    return codeRepository.save(newCode);
                });
    }

    /**
     * Tracks a new referral application with anti-fraud validations.
     */
    @Transactional
    public ReferralRecord trackReferral(String referralCodeStr, String refereeId, String refereeUpiId, String deviceId, String ipAddress) {
        log.info("Tracking referral code: {} for referee: {} | device: {} | IP: {}", 
                referralCodeStr, refereeId, deviceId, ipAddress);

        ReferralCode referrerCodeObj = codeRepository.findByCode(referralCodeStr)
                .orElseThrow(() -> new IllegalArgumentException("Invalid referral code: " + referralCodeStr));

        if (!referrerCodeObj.isActive()) {
            throw new IllegalArgumentException("Referral code is inactive: " + referralCodeStr);
        }

        String referrerId = referrerCodeObj.getUserId();

        // Anti-Fraud check 1: Same user can't refer themselves
        if (refereeId.equals(referrerId)) {
            throw new IllegalArgumentException("Self-referral is not allowed");
        }

        // Anti-Fraud check 2: Same device can't refer itself (used by referrer already)
        if (recordRepository.existsByReferrerIdAndDeviceId(referrerId, deviceId)) {
            throw new IllegalArgumentException("Fraud detected: Device already used under this referrer");
        }

        // Anti-Fraud check 3: Referee can only be referred once
        if (recordRepository.existsByRefereeId(refereeId)) {
            throw new IllegalArgumentException("User has already been referred");
        }

        // Anti-Fraud check 4: Same IP address can't refer more than 3 accounts
        long ipCount = recordRepository.countByIpAddress(ipAddress);
        if (ipCount >= 3) {
            throw new IllegalArgumentException("Fraud detected: IP address referral limit exceeded");
        }

        // Create Referral Record
        String referralId = "REF" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();

        ReferralRecord record = ReferralRecord.builder()
                .referralId(referralId)
                .referralCode(referralCodeStr)
                .referrerId(referrerId)
                .referrerUpiId(referrerCodeObj.getUserUpiId())
                .refereeId(refereeId)
                .refereeUpiId(refereeUpiId)
                .status(ReferralStatus.PENDING)
                .referrerRewardAmount(BigDecimal.valueOf(50.00)) // Referrer gets ₹50
                .refereeRewardAmount(BigDecimal.valueOf(25.00))  // Referee gets ₹25
                .referrerRewardStatus(ReferralRewardStatus.PENDING)
                .refereeRewardStatus(ReferralRewardStatus.PENDING)
                .deviceId(deviceId)
                .ipAddress(ipAddress)
                .build();

        record = recordRepository.save(record);

        // Update statistics
        referrerCodeObj.setTotalReferrals(referrerCodeObj.getTotalReferrals() + 1);
        codeRepository.save(referrerCodeObj);

        // Evict leaderboard cache to keep it fresh
        evictLeaderboardCache();

        log.info("Referral logged as PENDING. Referral ID: {}", referralId);
        return record;
    }

    /**
     * Qualifies a pending referral if the referee's first transaction meets the ₹100 threshold.
     */
    @Transactional
    public void qualifyReferral(String refereeId, String transactionId, BigDecimal amount) {
        log.info("Evaluating qualification for referee: {} | txn: {} | amount: ₹{}", refereeId, transactionId, amount);

        recordRepository.findByRefereeIdAndStatus(refereeId, ReferralStatus.PENDING).ifPresent(record -> {
            if (amount.compareTo(BigDecimal.valueOf(100.00)) >= 0) {
                record.setStatus(ReferralStatus.QUALIFIED);
                record.setRefereeFirstTransactionId(transactionId);
                record.setRefereeFirstTransactionAt(LocalDateTime.now());
                record.setQualifiedAt(LocalDateTime.now());
                record.setRewardScheduledAt(LocalDateTime.now().plus(Duration.ofMillis(rewardDelayMs)));
                recordRepository.save(record);

                // Update Referrer code metrics
                codeRepository.findByCode(record.getReferralCode()).ifPresent(codeObj -> {
                    codeObj.setQualifiedReferrals(codeObj.getQualifiedReferrals() + 1);
                    codeRepository.save(codeObj);
                });

                evictLeaderboardCache();
                log.info("Referral ID {} is now QUALIFIED. Rewards scheduled for credit in {} ms.", 
                        record.getReferralId(), rewardDelayMs);
            } else {
                log.info("Transaction amount ₹{} below ₹100 qualification limit for referee: {}", amount, refereeId);
            }
        });
    }

    /**
     * Credits referral rewards via rewards-service Feign call.
     */
    @Transactional
    public void creditReferralRewards(String referralId) {
        log.info("Crediting rewards for referral ID: {}", referralId);

        ReferralRecord record = recordRepository.findByReferralId(referralId)
                .orElseThrow(() -> new IllegalArgumentException("Referral record not found: " + referralId));

        if (record.getStatus() != ReferralStatus.QUALIFIED) {
            throw new IllegalStateException("Referral must be in QUALIFIED status to credit rewards. Current: " + record.getStatus());
        }

        try {
            ApplyReferralRequest request = ApplyReferralRequest.builder()
                    .newUserId(record.getRefereeId())
                    .referralCode(record.getReferralCode())
                    .build();

            // Calling rewards-service feign client
            RewardsApiResponse<Void> apiResponse = rewardsServiceClient.applyReferral(internalKey, request);
            
            if (apiResponse == null || !apiResponse.isSuccess()) {
                String error = apiResponse != null ? apiResponse.getMessage() : "Null response from rewards-service";
                throw new RuntimeException("Rewards-service credit rejected: " + error);
            }

            // Update statuses to PAID and REWARDED
            record.setStatus(ReferralStatus.REWARDED);
            record.setReferrerRewardStatus(ReferralRewardStatus.PAID);
            record.setRefereeRewardStatus(ReferralRewardStatus.PAID);
            recordRepository.save(record);

            // Update total referrer earnings
            codeRepository.findByCode(record.getReferralCode()).ifPresent(codeObj -> {
                codeObj.setTotalEarned(codeObj.getTotalEarned().add(record.getReferrerRewardAmount()));
                codeRepository.save(codeObj);
            });

            log.info("Rewards credited successfully for referral ID: {}", referralId);

        } catch (Exception e) {
            log.error("Failed to credit referral rewards for: {}", referralId, e);
            throw new RuntimeException("Rewards credit failed: " + e.getMessage(), e);
        }
    }

    /**
     * Scheduled job to automatically process qualified rewards older than 24h.
     */
    @Scheduled(fixedDelay = 60000) // Runs every 60 seconds
    @Transactional
    public void autoCreditQualifiedRewards() {
        LocalDateTime now = LocalDateTime.now();
        List<ReferralRecord> pendingCredits = recordRepository
                .findByStatusAndRewardScheduledAtBefore(ReferralStatus.QUALIFIED, now);

        if (!pendingCredits.isEmpty()) {
            log.info("Found {} qualified referrals ready for reward processing.", pendingCredits.size());
            for (ReferralRecord record : pendingCredits) {
                try {
                    creditReferralRewards(record.getReferralId());
                } catch (Exception e) {
                    log.error("Error auto-crediting rewards for referral: {}", record.getReferralId(), e);
                }
            }
        }
    }

    /**
     * Gets referral stats for a user.
     */
    public ReferralStatsResponse getReferralStats(String userId) {
        return codeRepository.findByUserId(userId)
                .map(code -> ReferralStatsResponse.builder()
                        .referralCode(code.getCode())
                        .totalReferrals(code.getTotalReferrals())
                        .qualifiedReferrals(code.getQualifiedReferrals())
                        .totalEarned(code.getTotalEarned())
                        .build())
                .orElseGet(() -> ReferralStatsResponse.builder()
                        .referralCode(null)
                        .totalReferrals(0)
                        .qualifiedReferrals(0)
                        .totalEarned(BigDecimal.ZERO)
                        .build());
    }

    /**
     * Returns top 10 users ordered by referral count. Utilizes Redis caching.
     */
    public List<LeaderboardEntry> getLeaderboard() {
        try {
            String cachedLeaderboard = (String) redisTemplate.opsForValue().get(LEADERBOARD_CACHE_KEY);
            if (cachedLeaderboard != null) {
                log.info("Leaderboard cache HIT in Redis.");
                return objectMapper.readValue(cachedLeaderboard, new TypeReference<List<LeaderboardEntry>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to fetch leaderboard from Redis cache", e);
        }

        // Cache miss: Load from database
        log.info("Leaderboard cache MISS. Fetching from database.");
        List<ReferralCode> topCodes = codeRepository.findTop10ByOrderByTotalReferralsDesc();
        List<LeaderboardEntry> entries = new ArrayList<>();

        for (int i = 0; i < topCodes.size(); i++) {
            ReferralCode code = topCodes.get(i);
            entries.add(LeaderboardEntry.builder()
                    .userId(code.getUserId())
                    .userUpiId(code.getUserUpiId())
                    .referralCode(code.getCode())
                    .totalReferrals(code.getTotalReferrals())
                    .qualifiedReferrals(code.getQualifiedReferrals())
                    .rank(i + 1)
                    .build());
        }

        // Write to Redis cache
        try {
            String json = objectMapper.writeValueAsString(entries);
            redisTemplate.opsForValue().set(LEADERBOARD_CACHE_KEY, json, Duration.ofHours(1));
            log.info("Leaderboard successfully cached in Redis.");
        } catch (Exception e) {
            log.error("Failed to write leaderboard to Redis cache", e);
        }

        return entries;
    }

    private void evictLeaderboardCache() {
        try {
            redisTemplate.delete(LEADERBOARD_CACHE_KEY);
            log.info("Evicted leaderboard cache.");
        } catch (Exception e) {
            log.error("Failed to evict leaderboard cache", e);
        }
    }
}
