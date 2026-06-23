package com.upimesh.rewards.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upimesh.rewards.exception.UserRewardAccountNotFoundException;
import com.upimesh.rewards.model.entity.RewardLedger;
import com.upimesh.rewards.model.entity.UserRewardAccount;
import com.upimesh.rewards.model.enums.RewardStatus;
import com.upimesh.rewards.model.enums.RewardType;
import com.upimesh.rewards.model.response.RewardBalanceResponse;
import com.upimesh.rewards.model.response.RewardLedgerResponse;
import com.upimesh.rewards.model.response.RedeemPointsResponse;
import com.upimesh.rewards.repository.RewardLedgerRepository;
import com.upimesh.rewards.repository.UserRewardAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardsService {

    private final UserRewardAccountRepository accountRepository;
    private final RewardLedgerRepository ledgerRepository;
    private final PointsCalculationService calculationService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rewards.credit-delay-ms:86400000}") // Default 24 hours in milliseconds
    private long creditDelayMs;

    /**
     * Credits points to a user's account for a transaction.
     * Starts as PENDING and updates availablePoints immediately. Transitions to CREDITED asynchronously after 24h.
     */
    @Transactional
    public RewardLedgerResponse creditPointsForTransaction(String transactionId, String userUpiId, BigDecimal amount) {
        log.info("Crediting points for transaction: {} | user: {} | amount: {}", transactionId, userUpiId, amount);

        int points = calculationService.calculatePoints(amount);

        UserRewardAccount account = getOrCreateAccount(userUpiId);

        String ledgerId = "LED" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();

        RewardLedger ledger = RewardLedger.builder()
                .ledgerId(ledgerId)
                .userId(account.getUserId())
                .userUpiId(userUpiId)
                .transactionId(transactionId)
                .rewardType(RewardType.POINTS)
                .points(points)
                .cashbackAmount(BigDecimal.ZERO)
                .status(RewardStatus.PENDING)
                .description("Points earned for transaction " + transactionId)
                .expiresAt(LocalDateTime.now().plusYears(1))
                .build();

        ledger = ledgerRepository.save(ledger);

        // Update DB Account Available Points
        account.setAvailablePoints(account.getAvailablePoints() + points);
        account.setTotalPointsEarned(account.getTotalPointsEarned() + points);
        account = accountRepository.save(account);

        // Update Redis Cache
        updateRedisCache(account);

        log.info("Points credited immediately (PENDING status) for ledger: {}. Points: {}", ledgerId, points);

        // Trigger Async credit task to transition status to CREDITED
        triggerAsyncCreditTransition(ledgerId);

        return mapToLedgerResponse(ledger);
    }

    /**
     * Asynchronously transitions a pending ledger status to CREDITED after the configured delay.
     */
    @Async
    public void triggerAsyncCreditTransition(String ledgerId) {
        try {
            if (creditDelayMs > 0) {
                log.info("Async points credit delayed by {} ms for ledger: {}", creditDelayMs, ledgerId);
                Thread.sleep(creditDelayMs);
            }
            
            // Execute state update
            moveLedgerToCredited(ledgerId);
        } catch (InterruptedException e) {
            log.error("Async credit transition interrupted for ledger: {}", ledgerId, e);
            Thread.currentThread().interrupt();
        }
    }

    @Transactional
    public void moveLedgerToCredited(String ledgerId) {
        ledgerRepository.findByLedgerId(ledgerId).ifPresent(ledger -> {
            if (ledger.getStatus() == RewardStatus.PENDING) {
                ledger.setStatus(RewardStatus.CREDITED);
                ledgerRepository.save(ledger);
                log.info("Async transition complete: Ledger {} moved to CREDITED", ledgerId);
            }
        });
    }

    /**
     * Redeems points for cashback (1 point = ₹0.25).
     */
    @Transactional
    public RedeemPointsResponse redeemPoints(String userId, int pointsToRedeem) {
        log.info("Redeeming {} points for user: {}", pointsToRedeem, userId);

        UserRewardAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new UserRewardAccountNotFoundException("User reward account not found for user: " + userId));

        if (account.getAvailablePoints() < pointsToRedeem) {
            throw new IllegalArgumentException("Insufficient points balance. Available: " 
                    + account.getAvailablePoints() + ", Requested: " + pointsToRedeem);
        }

        BigDecimal cashbackAmount = BigDecimal.valueOf(pointsToRedeem)
                .multiply(BigDecimal.valueOf(0.25))
                .setScale(2, RoundingMode.HALF_UP);

        // Update Account details
        account.setAvailablePoints(account.getAvailablePoints() - pointsToRedeem);
        account.setTotalPointsRedeemed(account.getTotalPointsRedeemed() + pointsToRedeem);
        account.setTotalCashbackEarned(account.getTotalCashbackEarned().add(cashbackAmount));
        account = accountRepository.save(account);

        // Update Redis Cache
        updateRedisCache(account);

        String ledgerId = "LED" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();

        RewardLedger ledger = RewardLedger.builder()
                .ledgerId(ledgerId)
                .userId(userId)
                .userUpiId(account.getUserUpiId())
                .rewardType(RewardType.CASHBACK)
                .points(pointsToRedeem)
                .cashbackAmount(cashbackAmount)
                .status(RewardStatus.REDEEMED)
                .description("Redeemed " + pointsToRedeem + " points for ₹" + cashbackAmount + " cashback")
                .build();

        ledgerRepository.save(ledger);
        log.info("Redemption successful. Cashback ₹{} credited for user {}", cashbackAmount, userId);

        return RedeemPointsResponse.builder()
                .userId(userId)
                .pointsRedeemed(pointsToRedeem)
                .cashbackAmount(cashbackAmount)
                .availablePoints(account.getAvailablePoints())
                .build();
    }

    /**
     * Applies referral reward logic: credit 100 points to referrer, 50 points to referred user.
     */
    @Transactional
    public void applyReferral(String newUserId, String referralCode) {
        log.info("Applying referral code: {} for new user: {}", referralCode, newUserId);

        UserRewardAccount referrer = accountRepository.findByReferralCode(referralCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid referral code: " + referralCode));

        if (referrer.getUserId().equals(newUserId)) {
            throw new IllegalArgumentException("Self-referral is not allowed");
        }

        // Get or create new user's reward account
        String newUserUpiId = newUserId + "@upimesh";
        UserRewardAccount newUser = accountRepository.findByUserId(newUserId)
                .orElse(null);

        if (newUser == null) {
            newUser = UserRewardAccount.builder()
                    .userId(newUserId)
                    .userUpiId(newUserUpiId)
                    .totalPointsEarned(0)
                    .totalPointsRedeemed(0)
                    .totalCashbackEarned(BigDecimal.ZERO)
                    .availablePoints(0)
                    .referralCode(generateUniqueReferralCode())
                    .build();
        }

        if (newUser.getReferredByUserId() != null) {
            throw new IllegalStateException("Referral bonus has already been claimed by user: " + newUserId);
        }

        newUser.setReferredByUserId(referrer.getUserId());

        // 1. Reward Referrer: 100 points
        referrer.setAvailablePoints(referrer.getAvailablePoints() + 100);
        referrer.setTotalPointsEarned(referrer.getTotalPointsEarned() + 100);
        accountRepository.save(referrer);
        updateRedisCache(referrer);

        String refLedgerId = "LED" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();
        RewardLedger refLedger = RewardLedger.builder()
                .ledgerId(refLedgerId)
                .userId(referrer.getUserId())
                .userUpiId(referrer.getUserUpiId())
                .rewardType(RewardType.POINTS)
                .points(100)
                .cashbackAmount(BigDecimal.ZERO)
                .status(RewardStatus.CREDITED)
                .description("Referral bonus for referring user " + newUserId)
                .build();
        ledgerRepository.save(refLedger);

        // 2. Reward New User: 50 points
        newUser.setAvailablePoints(newUser.getAvailablePoints() + 50);
        newUser.setTotalPointsEarned(newUser.getTotalPointsEarned() + 50);
        accountRepository.save(newUser);
        updateRedisCache(newUser);

        String userLedgerId = "LED" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 15).toUpperCase();
        RewardLedger userLedger = RewardLedger.builder()
                .ledgerId(userLedgerId)
                .userId(newUser.getUserId())
                .userUpiId(newUser.getUserUpiId())
                .rewardType(RewardType.POINTS)
                .points(50)
                .cashbackAmount(BigDecimal.ZERO)
                .status(RewardStatus.CREDITED)
                .description("Signup referral bonus from code " + referralCode)
                .build();
        ledgerRepository.save(userLedger);

        log.info("Referral code applied successfully. Referrer: {} (+100 pts), Referred: {} (+50 pts)", 
                referrer.getUserId(), newUserId);
    }

    /**
     * Gets user reward balance. Checks Redis first, falls back to DB.
     */
    public RewardBalanceResponse getUserBalance(String userId) {
        String key = "rewards:account:" + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("Cache hit in Redis for user rewards: {}", userId);
                return objectMapper.convertValue(cached, RewardBalanceResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to read user rewards from Redis cache: {}", userId, e);
        }

        // Cache miss: load from DB
        UserRewardAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new UserRewardAccountNotFoundException("Rewards account not found for user: " + userId));

        RewardBalanceResponse response = mapToBalanceResponse(account);

        // Cache in Redis
        try {
            redisTemplate.opsForValue().set(key, response, Duration.ofHours(1));
        } catch (Exception e) {
            log.error("Failed to write user rewards to Redis cache: {}", userId, e);
        }

        return response;
    }

    public Page<RewardLedgerResponse> getRewardHistory(String userId, Pageable pageable) {
        return ledgerRepository.findByUserId(userId, pageable)
                .map(this::mapToLedgerResponse);
    }

    private UserRewardAccount getOrCreateAccount(String userUpiId) {
        return accountRepository.findByUserUpiId(userUpiId)
                .orElseGet(() -> {
                    String userId = userUpiId.split("@")[0];
                    UserRewardAccount newAccount = UserRewardAccount.builder()
                            .userId(userId)
                            .userUpiId(userUpiId)
                            .totalPointsEarned(0)
                            .totalPointsRedeemed(0)
                            .totalCashbackEarned(BigDecimal.ZERO)
                            .availablePoints(0)
                            .referralCode(generateUniqueReferralCode())
                            .build();
                    return accountRepository.save(newAccount);
                });
    }

    private void updateRedisCache(UserRewardAccount account) {
        String key = "rewards:account:" + account.getUserId();
        try {
            RewardBalanceResponse response = mapToBalanceResponse(account);
            redisTemplate.opsForValue().set(key, response, Duration.ofHours(1));
        } catch (Exception e) {
            log.error("Failed to update user rewards Redis cache: {}", account.getUserId(), e);
        }
    }

    private String generateUniqueReferralCode() {
        return UUID.randomUUID().toString().replace("-", "")
                .substring(0, 10).toUpperCase();
    }

    private RewardBalanceResponse mapToBalanceResponse(UserRewardAccount account) {
        return RewardBalanceResponse.builder()
                .userId(account.getUserId())
                .userUpiId(account.getUserUpiId())
                .totalPointsEarned(account.getTotalPointsEarned())
                .totalPointsRedeemed(account.getTotalPointsRedeemed())
                .totalCashbackEarned(account.getTotalCashbackEarned())
                .availablePoints(account.getAvailablePoints())
                .referralCode(account.getReferralCode())
                .build();
    }

    private RewardLedgerResponse mapToLedgerResponse(RewardLedger ledger) {
        return RewardLedgerResponse.builder()
                .ledgerId(ledger.getLedgerId())
                .userId(ledger.getUserId())
                .userUpiId(ledger.getUserUpiId())
                .transactionId(ledger.getTransactionId())
                .rewardType(ledger.getRewardType())
                .points(ledger.getPoints())
                .cashbackAmount(ledger.getCashbackAmount())
                .status(ledger.getStatus())
                .description(ledger.getDescription())
                .expiresAt(ledger.getExpiresAt())
                .createdAt(ledger.getCreatedAt())
                .build();
    }
}
