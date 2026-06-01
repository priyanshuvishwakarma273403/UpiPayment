package com.rewards_service.Rewards.service;

import com.rewards_service.Rewards.entity.RewardAccount;
import com.rewards_service.Rewards.entity.RewardTransaction;
import com.rewards_service.Rewards.exception.RewardsException;
import com.rewards_service.Rewards.repository.RewardAccountRepository;
import com.rewards_service.Rewards.repository.RewardTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ================================================================
 * Rewards Service - Points and Cashback Logic
 * ================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RewardsService {

    private final RewardAccountRepository     accountRepository;
    private final RewardTransactionRepository transactionRepository;

    private static final long POINTS_PER_10_RUPEES   = 1L;
    private static final long SIGNUP_BONUS_POINTS     = 100L;
    private static final long MIN_REDEMPTION_POINTS   = 500L;
    private static final BigDecimal POINTS_TO_INR     = new BigDecimal("0.01"); // 100 pts = ₹1

    /** Naya reward account banao (user registration ke baad) */
    @Transactional
    public RewardAccount createAccount(Long userId) {
        if (accountRepository.existsByUserId(userId))
            return accountRepository.findByUserId(userId).orElseThrow();

        RewardAccount account = RewardAccount.builder().userId(userId).build();
        account = accountRepository.save(account);

        // Welcome bonus
        earnPoints(userId, SIGNUP_BONUS_POINTS, "WELCOME_BONUS", "Welcome bonus points!", RewardTransaction.RewardTxnType.EARNED_SIGNUP);
        log.info("Reward account created for userId={} with {} welcome points", userId, SIGNUP_BONUS_POINTS);
        return account;
    }

    /** Payment complete hone par points earn karo */
    @Transactional
    public void earnPointsForPayment(Long userId, BigDecimal amount, String paymentId) {
        // Idempotency - same payment dobara process nahi
        if (transactionRepository.existsByReferenceIdAndTransactionType(
                paymentId, RewardTransaction.RewardTxnType.EARNED_PAYMENT)) {
            log.warn("Points already awarded for paymentId={}", paymentId);
            return;
        }

        long points = (amount.divide(BigDecimal.TEN, 0, RoundingMode.DOWN).longValue()) * POINTS_PER_10_RUPEES;
        if (points <= 0) return;

        earnPoints(userId, points, paymentId, "Payment reward: ₹" + amount, RewardTransaction.RewardTxnType.EARNED_PAYMENT);
        log.info("Earned {} points for userId={}, paymentId={}", points, userId, paymentId);
    }

    /** Points ko wallet cashback mein redeem karo */
    @Transactional
    public Map<String, Object> redeemPoints(Long userId, Long pointsToRedeem) {
        RewardAccount account = getAccountOrCreate(userId);

        if (pointsToRedeem < MIN_REDEMPTION_POINTS)
            throw new RewardsException("Minimum redemption is " + MIN_REDEMPTION_POINTS + " points.");

        if (account.getAvailablePoints() < pointsToRedeem)
            throw new RewardsException("Insufficient points. Available: " + account.getAvailablePoints());

        BigDecimal cashback = new BigDecimal(pointsToRedeem).multiply(POINTS_TO_INR).setScale(2, RoundingMode.HALF_UP);

        account.setRedeemedPoints(account.getRedeemedPoints() + pointsToRedeem);
        account.setAvailablePoints(account.getAvailablePoints() - pointsToRedeem);
        account.setCashbackBalance(account.getCashbackBalance().add(cashback));
        account.setTotalCashbackEarned(account.getTotalCashbackEarned().add(cashback));
        accountRepository.save(account);

        RewardTransaction txn = RewardTransaction.builder()
                .userId(userId).transactionType(RewardTransaction.RewardTxnType.REDEEMED_CASHBACK)
                .pointsChange(-pointsToRedeem).cashbackAmount(cashback)
                .description("Redeemed " + pointsToRedeem + " points for ₹" + cashback + " cashback")
                .balanceAfterPoints(account.getAvailablePoints()).build();
        transactionRepository.save(txn);

        // TODO: Credit cashback to wallet via Feign call
        log.info("Redeemed {} points -> ₹{} cashback for userId={}", pointsToRedeem, cashback, userId);

        return Map.of("success", true, "pointsRedeemed", pointsToRedeem,
                "cashbackAmount", cashback, "availablePoints", account.getAvailablePoints());
    }

    @Transactional(readOnly = true)
    public RewardAccount getBalance(Long userId) {
        return getAccountOrCreate(userId);
    }

    @Transactional(readOnly = true)
    public Page<RewardTransaction> getHistory(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    // ---- Private Helpers ----

    private void earnPoints(Long userId, Long points, String refId, String desc, RewardTransaction.RewardTxnType type) {
        RewardAccount account = getAccountOrCreate(userId);
        account.setTotalPoints(account.getTotalPoints() + points);
        account.setAvailablePoints(account.getAvailablePoints() + points);
        updateTier(account);
        accountRepository.save(account);

        RewardTransaction txn = RewardTransaction.builder()
                .userId(userId).referenceId(refId).transactionType(type)
                .pointsChange(points).description(desc)
                .expiryDate(LocalDateTime.now().plusYears(1))
                .balanceAfterPoints(account.getAvailablePoints()).build();
        transactionRepository.save(txn);
    }

    private void updateTier(RewardAccount account) {
        long total = account.getTotalPoints();
        String tier = total >= 50000 ? "PLATINUM" : total >= 20000 ? "GOLD"
                : total >= 5000  ? "SILVER"   : "BRONZE";
        account.setTier(tier);
    }

    private RewardAccount getAccountOrCreate(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseGet(() -> createAccount(userId));
    }
}
