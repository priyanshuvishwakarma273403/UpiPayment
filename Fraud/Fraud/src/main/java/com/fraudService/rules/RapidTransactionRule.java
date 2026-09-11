package com.fraudService.rules;

import com.fraudService.dto.request.FraudCheckRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Rapid Transaction Rule - Atomic Redis Based with Outage Resilience
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RapidTransactionRule implements FraudRule {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "vel:cust:";
    private static final int REVIEW_LIMIT = 5;
    private static final int BLOCK_LIMIT = 10;

    @Override
    public RuleResult evaluate(FraudCheckRequest request) {
        String key = PREFIX + request.getSenderId() + ":1m";

        try {
            Long countLong = redisTemplate.opsForValue().increment(key);
            long count = countLong != null ? countLong : 1L;

            if (count == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }

            if (count >= BLOCK_LIMIT) {
                return RuleResult.blocked("RapidTransactionRule",
                        count + " transactions in 1 minute (limit: " + BLOCK_LIMIT + ")");
            }

            if (count >= REVIEW_LIMIT) {
                return RuleResult.review("RapidTransactionRule",
                        count + " transactions in 1 minute", 0.6);
            }

            return RuleResult.safe("RapidTransactionRule");

        } catch (Exception e) {
            log.warn("Redis outage in RapidTransactionRule: {}. Applying fallback review risk.", e.getMessage());
            return RuleResult.review("RapidTransactionRule",
                    "Redis velocity tracking degraded due to outage: " + e.getMessage(), 0.3);
        }
    }
}
