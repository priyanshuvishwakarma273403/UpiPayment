package com.upimesh.subscription.controller;

import com.upimesh.subscription.model.request.CreateSubscriptionRequest;
import com.upimesh.subscription.model.response.ApiResponse;
import com.upimesh.subscription.model.response.PaymentAttemptResponse;
import com.upimesh.subscription.model.response.SubscriptionResponse;
import com.upimesh.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        log.info("REST request to create subscription for user: {}", request.getUserId());
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription mandate initiated successfully"));
    }

    @PutMapping("/{subscriptionId}/pause")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> pauseSubscription(
            @PathVariable String subscriptionId) {
        log.info("REST request to pause subscription: {}", subscriptionId);
        SubscriptionResponse response = subscriptionService.pauseSubscription(subscriptionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription paused successfully"));
    }

    @PutMapping("/{subscriptionId}/resume")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> resumeSubscription(
            @PathVariable String subscriptionId) {
        log.info("REST request to resume subscription: {}", subscriptionId);
        SubscriptionResponse response = subscriptionService.resumeSubscription(subscriptionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription resumed successfully"));
    }

    @PutMapping("/{subscriptionId}/cancel")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @PathVariable String subscriptionId,
            @RequestParam(required = false, defaultValue = "User requested cancellation") String reason) {
        log.info("REST request to cancel subscription: {}", subscriptionId);
        SubscriptionResponse response = subscriptionService.cancelSubscription(subscriptionId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription cancelled successfully"));
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(
            @PathVariable String subscriptionId) {
        log.info("REST request to get subscription: {}", subscriptionId);
        SubscriptionResponse response = subscriptionService.getSubscription(subscriptionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription details fetched successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getUserSubscriptions(
            @PathVariable String userId) {
        log.info("REST request to get all subscriptions for user: {}", userId);
        List<SubscriptionResponse> responses = subscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(ApiResponse.success(responses, "User subscriptions fetched successfully"));
    }

    @GetMapping("/{subscriptionId}/attempts")
    public ResponseEntity<ApiResponse<List<PaymentAttemptResponse>>> getPaymentAttempts(
            @PathVariable String subscriptionId) {
        log.info("REST request to get payment attempts for subscription: {}", subscriptionId);
        List<PaymentAttemptResponse> responses = subscriptionService.getPaymentAttempts(subscriptionId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Payment attempts fetched successfully"));
    }

    @PostMapping("/execute-scheduled")
    public ResponseEntity<ApiResponse<Void>> executeScheduledPayments() {
        log.info("REST request to manually trigger scheduled payments");
        subscriptionService.executeScheduledPayments();
        return ResponseEntity.ok(ApiResponse.success(null, "Scheduled payments executed successfully"));
    }
}
