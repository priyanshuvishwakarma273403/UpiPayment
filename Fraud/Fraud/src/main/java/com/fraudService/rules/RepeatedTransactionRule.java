package com.fraudService.rules;

import com.fraudService.dto.request.FraudCheckRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * ================================================================
 * Repeated Transaction Rule
 * ================================================================
 * 5 minute mein same receiver ko same amount = Suspicious
 */
@Component
@RequiredArgsConstructor
public class RepeatedTransactionRule implements FraudRule{

    private final StringRedisTemplate redisTemplate;

    @Override
    public RuleResult evaluate(FraudCheckRequest request) {

        // Key: sender + receiver + amount (unique combination)
        String key = String.format("repeat:%d:%s:%s",
                request.getSenderId(),
                request.getReceiverUpiId(),
                request.getAmount().toPlainString()
        );

        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            return RuleResult.review("RepeatedTransactionRule",
                    "Same amount to same receiver within 5 minutes", 0.55);
        }

        // 5 minute ke liye mark karo
        redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(5));
        return RuleResult.safe("RepeatedTransactionRule");

    }
}
