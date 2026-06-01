package com.rewards_service.Rewards.contoller;

import com.rewards_service.Rewards.entity.RewardAccount;
import com.rewards_service.Rewards.entity.RewardTransaction;
import com.rewards_service.Rewards.service.RewardsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
@Tag(name = "Rewards", description = "Points, cashback and offers management")
public class RewardsController {

    private final RewardsService rewardsService;

    @GetMapping("/balance")
    @Operation(summary = "Get rewards points balance and tier")
    public ResponseEntity<RewardAccount> getBalance(@RequestHeader("X-User-Id") String userId){
        return ResponseEntity.ok(rewardsService.getBalance(Long.parseLong(userId)));
    }

    @GetMapping("/history")
    @Operation(summary = "Get reward transaction history")
    public ResponseEntity<Page<RewardTransaction>> getHistory(

            @RequestHeader("X-User-Id") String userId,
            @PageableDefault(size = 20)Pageable pageable ){
        return ResponseEntity.ok(rewardsService.getHistory(Long.parseLong(userId), pageable));
    }


    @PostMapping("/redeem")
    @Operation(summary = "Redeem points for cashback to wallet")
    public ResponseEntity<Map<String, Object>> redeemPoints(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Long> body) {
        return ResponseEntity.ok(rewardsService.redeemPoints(Long.parseLong(userId), body.get("points")));
    }
}
