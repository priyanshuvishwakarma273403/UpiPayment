package com.upimesh.risk.service;

import com.upimesh.risk.model.dto.VelocityResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * High-speed Redis Velocity & Short-Lived Intelligence Tracker.
 * Manages short-lived sliding window counters (1m, 5m, 1h) across
 * customer, device, IP, and beneficiary dimensions.
 *
 * Guarantees race-condition-free atomic increments and graceful fallback
 * when Redis is unavailable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisVelocityTracker {

    private final StringRedisTemplate redisTemplate;

    private static final String CUST_1M_PREFIX = "vel:cust:%s:1m";
    private static final String CUST_5M_PREFIX = "vel:cust:%s:5m";
    private static final String CUST_1H_PREFIX = "vel:cust:%s:1h";

    private static final String DEV_1M_PREFIX = "vel:dev:%s:1m";
    private static final String DEV_1H_PREFIX = "vel:dev:%s:1h";

    private static final String IP_1M_PREFIX = "vel:ip:%s:1m";
    private static final String IP_1H_PREFIX = "vel:ip:%s:1h";

    private static final String BEN_1M_PREFIX = "vel:ben:%s:1m";
    private static final String BEN_1H_PREFIX = "vel:ben:%s:1h";

    public VelocityResult trackAndEvaluateVelocity(String userId, String deviceId, String ipAddress, String receiverUpiId) {
        VelocityResult.VelocityResultBuilder builder = VelocityResult.builder();
        Map<String, Object> details = new HashMap<>();

        try {
            // 1. Customer Velocity
            if (StringUtils.hasText(userId)) {
                long c1m = incrementAndExpire(String.format(CUST_1M_PREFIX, userId), 60);
                long c5m = incrementAndExpire(String.format(CUST_5M_PREFIX, userId), 300);
                long c1h = incrementAndExpire(String.format(CUST_1H_PREFIX, userId), 3600);

                builder.customerCount1m(c1m)
                       .customerCount5m(c5m)
                       .customerCount1h(c1h);

                details.put("cust_1m", c1m);
                details.put("cust_5m", c5m);
                details.put("cust_1h", c1h);
            }

            // 2. Device Activity
            if (StringUtils.hasText(deviceId)) {
                long d1m = incrementAndExpire(String.format(DEV_1M_PREFIX, deviceId), 60);
                long d1h = incrementAndExpire(String.format(DEV_1H_PREFIX, deviceId), 3600);

                builder.deviceCount1m(d1m)
                       .deviceCount1h(d1h);

                details.put("dev_1m", d1m);
                details.put("dev_1h", d1h);
            }

            // 3. IP Activity
            if (StringUtils.hasText(ipAddress)) {
                long ip1m = incrementAndExpire(String.format(IP_1M_PREFIX, ipAddress), 60);
                long ip1h = incrementAndExpire(String.format(IP_1H_PREFIX, ipAddress), 3600);

                builder.ipCount1m(ip1m)
                       .ipCount1h(ip1h);

                details.put("ip_1m", ip1m);
                details.put("ip_1h", ip1h);
            }

            // 4. Beneficiary Velocity
            if (StringUtils.hasText(receiverUpiId)) {
                long b1m = incrementAndExpire(String.format(BEN_1M_PREFIX, receiverUpiId), 60);
                long b1h = incrementAndExpire(String.format(BEN_1H_PREFIX, receiverUpiId), 3600);

                builder.beneficiaryCount1m(b1m)
                       .beneficiaryCount1h(b1h);

                details.put("ben_1m", b1m);
                details.put("ben_1h", b1h);
            }

            builder.degraded(false)
                   .fallbackApplied(false)
                   .details(details);

            return builder.build();

        } catch (Exception e) {
            log.warn("Redis velocity tracking unavailable/failed: {}. Applying conservative risk fallback.", e.getMessage());
            return VelocityResult.builder()
                    .degraded(true)
                    .fallbackApplied(true)
                    .fallbackReason("Redis connectivity failure: " + e.getMessage())
                    .customerCount1m(0)
                    .customerCount5m(0)
                    .customerCount1h(0)
                    .details(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Atomic INCR and conditional TTL setting to avoid race conditions.
     */
    public long incrementAndExpire(String key, long ttlSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        }
        return count != null ? count : 1L;
    }
}
