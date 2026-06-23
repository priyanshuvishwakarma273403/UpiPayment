package com.upimesh.referral.feign;

import com.upimesh.referral.feign.dto.ApplyReferralRequest;
import com.upimesh.referral.feign.dto.RewardsApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "rewards-service")
public interface RewardsServiceClient {

    @PostMapping("/rewards/referral/apply")
    RewardsApiResponse<Void> applyReferral(
            @RequestHeader("X-Internal-Service-Key") String serviceKey,
            @RequestBody ApplyReferralRequest request
    );
}
