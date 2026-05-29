package com.fraudService.rules;

import com.fraudService.dto.request.FraudCheckRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * ================================================================
 * Rapid Transaction Rule - Redis Based
 * ================================================================
 * 1 minute mein 5 se zyada transactions = Review
 * 1 minute mein 10 se zyada = Blocked
 */
@Component
@RequiredArgsConstructor
public class RapidTransactionRule implements FraudRule{

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "txn_count:";
    private static final int REVIEW_LIMIT = 5;
    private static final int BLOCK_LIMIT = 10;


    @Override
    public RuleResult evaluate(FraudCheckRequest request) {
        String key = PREFIX + request.getSenderId();
        String countStr = redisTemplate.opsForValue().get(key);
        int count  = countStr != null ? Integer.parseInt(countStr) : 0;

        // Counter increment karo (1 minute TTL)
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(1));

        if(count >= BLOCK_LIMIT) {
            return RuleResult.blocked("RapidTransactionRule",
                    count + "transactions in 1 minute (limit : " + BLOCK_LIMIT + ")");
        }

        if (count >= REVIEW_LIMIT) {
            return RuleResult.review("RapidTransactionRule",
                    count + " transactions in 1 minute", 0.6);
        }
        return RuleResult.safe("RapidTransactionRule");
    }
}
