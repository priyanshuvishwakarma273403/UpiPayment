package com.upimesh.rewards.controller;

import com.upimesh.rewards.model.request.ApplyReferralRequest;
import com.upimesh.rewards.model.request.CreditPointsRequest;
import com.upimesh.rewards.model.request.RedeemPointsRequest;
import com.upimesh.rewards.model.response.ApiResponse;
import com.upimesh.rewards.model.response.RedeemPointsResponse;
import com.upimesh.rewards.model.response.RewardBalanceResponse;
import com.upimesh.rewards.model.response.RewardLedgerResponse;
import com.upimesh.rewards.service.RewardsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/rewards", "/reward"})
@RequiredArgsConstructor
@Slf4j
public class RewardsController {

    private final RewardsService rewardsService;

    @PostMapping("/credit")
    public ResponseEntity<ApiResponse<RewardLedgerResponse>> creditPoints(@Valid @RequestBody CreditPointsRequest request) {
        log.info("REST request to credit points | txnId={} | user={}", request.getTransactionId(), request.getUserUpiId());
        RewardLedgerResponse response = rewardsService.creditPointsForTransaction(
                request.getTransactionId(), request.getUserUpiId(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(response, "Points credited successfully"));
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<RedeemPointsResponse>> redeemPoints(@Valid @RequestBody RedeemPointsRequest request) {
        log.info("REST request to redeem points | user={} | points={}", request.getUserId(), request.getPointsToRedeem());
        RedeemPointsResponse response = rewardsService.redeemPoints(request.getUserId(), request.getPointsToRedeem());
        return ResponseEntity.ok(ApiResponse.success(response, "Points redeemed successfully"));
    }

    @PostMapping({"/referral/apply", "/referral-apply"})
    public ResponseEntity<ApiResponse<String>> applyReferral(@Valid @RequestBody ApplyReferralRequest request) {
        log.info("REST request to apply referral | newUser={} | code={}", request.getNewUserId(), request.getReferralCode());
        rewardsService.applyReferral(request.getNewUserId(), request.getReferralCode());
        return ResponseEntity.ok(ApiResponse.success("Referral applied successfully", "Referral applied successfully"));
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<ApiResponse<RewardBalanceResponse>> getBalance(@PathVariable String userId) {
        log.info("REST request to get reward balance | user={}", userId);
        RewardBalanceResponse response = rewardsService.getUserBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Reward balance retrieved"));
    }
}
