package com.syncService.Sync.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Feign client to call payment-service
 * Offline payments fetch karo aur process karo
 */
@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    @GetMapping("/payment/pending-sync")
    List<Map<String, Object>> getPendingSyncPayments(@RequestBody Map<String, Long> body);

    @PostMapping("/payment/offline-pay")
    Map<String, Object> processOfflinePayment(@RequestBody Map<String, Object> request);


}
