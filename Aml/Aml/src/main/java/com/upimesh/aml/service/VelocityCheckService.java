package com.upimesh.aml.service;

import com.upimesh.aml.model.dto.VelocityResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VelocityCheckService {

    private final StringRedisTemplate redisTemplate;

    @Value("${aml.velocity.per-hour-max:10}")
    private int perHourMax;

    @Value("${aml.velocity.per-day-max:25}")
    private int perDayMax;

    private static final String HOURLY_PREFIX = "vel:h:";
    private static final String DAILY_PREFIX = "vel:d:";

    public VelocityResult checkVelocity(String userUpiId, BigDecimal amount) {
        String hourlyKey = HOURLY_PREFIX + userUpiId;
        String dailyKey = DAILY_PREFIX + userUpiId;

        Long hourlyCountLong = redisTemplate.opsForValue().increment(hourlyKey);
        if (hourlyCountLong != null && hourlyCountLong == 1) {
            redisTemplate.expire(hourlyKey, 1, TimeUnit.HOURS);
        }
        int hourlyCount = hourlyCountLong != null ? hourlyCountLong.intValue() : 1;

        Long dailyCountLong = redisTemplate.opsForValue().increment(dailyKey);
        if (dailyCountLong != null && dailyCountLong == 1) {
            redisTemplate.expire(dailyKey, 1, TimeUnit.DAYS);
        }
        int dailyCount = dailyCountLong != null ? dailyCountLong.intValue() : 1;

        log.debug("Velocity check for user: {}, hourlyCount: {}/{}, dailyCount: {}/{}", 
                userUpiId, hourlyCount, perHourMax, dailyCount, perDayMax);

        if (hourlyCount > perHourMax) {
            return new VelocityResult(true, 
                    String.format("Hourly transaction limit exceeded: %d txns in last hour (max limit is %d)", hourlyCount, perHourMax), 
                    hourlyCount, dailyCount);
        }

        if (dailyCount > perDayMax) {
            return new VelocityResult(true, 
                    String.format("Daily transaction limit exceeded: %d txns in last day (max limit is %d)", dailyCount, perDayMax), 
                    hourlyCount, dailyCount);
        }

        return new VelocityResult(false, "Velocity limits within threshold", hourlyCount, dailyCount);
    }
}
