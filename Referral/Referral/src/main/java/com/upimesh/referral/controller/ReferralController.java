package com.upimesh.referral.controller;

import com.upimesh.referral.model.entity.ReferralCode;
import com.upimesh.referral.model.entity.ReferralRecord;
import com.upimesh.referral.model.request.QualifyReferralRequest;
import com.upimesh.referral.model.request.TrackReferralRequest;
import com.upimesh.referral.model.response.ApiResponse;
import com.upimesh.referral.model.response.LeaderboardEntry;
import com.upimesh.referral.model.response.ReferralStatsResponse;
import com.upimesh.referral.service.ReferralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/referral")
@RequiredArgsConstructor
@Slf4j
public class ReferralController {

    private final ReferralService referralService;

    /**
     * POST /referral/code/generate
     * Generates or retrieves a unique referral code for a user.
     */
    @PostMapping("/code/generate")
    public ResponseEntity<ApiResponse<ReferralCode>> generateReferralCode(
            @RequestParam("userId") String userId) {
        log.info("Request to generate referral code for user: {}", userId);
        ReferralCode referralCode = referralService.generateReferralCode(userId);
        return ResponseEntity.ok(ApiResponse.success(referralCode, "Referral code generated successfully"));
    }

    /**
     * POST /referral/track
     * Tracks a new referral record. Enforces anti-fraud device/IP validations.
     */
    @PostMapping("/track")
    public ResponseEntity<ApiResponse<ReferralRecord>> trackReferral(
            @Valid @RequestBody TrackReferralRequest request) {
        log.info("Request to track referral with code: {} | referee: {}", request.getReferralCode(), request.getRefereeId());
        ReferralRecord record = referralService.trackReferral(
                request.getReferralCode(),
                request.getRefereeId(),
                request.getRefereeUpiId(),
                request.getDeviceId(),
                request.getIpAddress()
        );
        return ResponseEntity.ok(ApiResponse.success(record, "Referral tracked successfully as PENDING"));
    }

    /**
     * POST /referral/qualify
     * Qualifies a pending referral once the referee completes their first transaction (>= ₹100).
     */
    @PostMapping("/qualify")
    public ResponseEntity<ApiResponse<Void>> qualifyReferral(
            @Valid @RequestBody QualifyReferralRequest request) {
        log.info("Request to qualify referral for referee: {}", request.getRefereeId());
        referralService.qualifyReferral(
                request.getRefereeId(),
                request.getTransactionId(),
                request.getAmount()
        );
        return ResponseEntity.ok(ApiResponse.success("Referral qualification process executed"));
    }

    /**
     * GET /referral/stats/{userId}
     * Retrieves campaign stats for a user.
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<ApiResponse<ReferralStatsResponse>> getReferralStats(
            @PathVariable String userId) {
        log.info("Request for referral stats for user: {}", userId);
        ReferralStatsResponse stats = referralService.getReferralStats(userId);
        return ResponseEntity.ok(ApiResponse.success(stats, "Referral stats fetched successfully"));
    }

    /**
     * GET /referral/leaderboard
     * Retrieves the top 10 referrers leaderboard.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getLeaderboard() {
        log.info("Request to fetch top 10 referrers leaderboard.");
        List<LeaderboardEntry> leaderboard = referralService.getLeaderboard();
        return ResponseEntity.ok(ApiResponse.success(leaderboard, "Leaderboard fetched successfully"));
    }

    /**
     * POST /referral/credit/{referralId}
     * Manually triggers reward credit for a qualified referral.
     */
    @PostMapping("/credit/{referralId}")
    public ResponseEntity<ApiResponse<Void>> creditRewards(
            @PathVariable String referralId) {
        log.info("Request to manually credit rewards for referral: {}", referralId);
        referralService.creditReferralRewards(referralId);
        return ResponseEntity.ok(ApiResponse.success("Rewards credited successfully"));
    }
}
