package com.upimesh.risk.service;

import com.upimesh.risk.model.dto.VelocityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisVelocityTrackerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisVelocityTracker velocityTracker;

    @BeforeEach
    void setUp() {
        velocityTracker = new RedisVelocityTracker(redisTemplate);
    }

    @Test
    void testVelocityTrackingSuccess() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        VelocityResult result = velocityTracker.trackAndEvaluateVelocity("user123", "dev456", "127.0.0.1", "merchant@upimesh");

        assertNotNull(result);
        assertFalse(result.isDegraded());
        assertFalse(result.isFallbackApplied());
        assertEquals(1L, result.getCustomerCount1m());
        assertEquals(1L, result.getCustomerCount5m());
        assertEquals(1L, result.getCustomerCount1h());

        // Verify TTL set when count is 1
        verify(redisTemplate, atLeastOnce()).expire(anyString(), any(Duration.class));
    }

    @Test
    void testRedisOutageFallbackGracefulDegradation() {
        when(redisTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Redis port 6379 unreachable"));

        VelocityResult result = velocityTracker.trackAndEvaluateVelocity("user123", "dev456", "127.0.0.1", "merchant@upimesh");

        assertNotNull(result);
        assertTrue(result.isDegraded());
        assertTrue(result.isFallbackApplied());
        assertNotNull(result.getFallbackReason());
        assertTrue(result.getFallbackReason().contains("Redis connectivity failure"));
    }

    @Test
    void testAtomicIncrementHelper() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("test_key")).thenReturn(1L);

        long count = velocityTracker.incrementAndExpire("test_key", 60);

        assertEquals(1L, count);
        verify(redisTemplate, times(1)).expire("test_key", Duration.ofSeconds(60));
    }

    @Test
    void testConcurrentAtomicIncrements() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        AtomicLong counter = new AtomicLong(0);

        when(valueOperations.increment(anyString())).thenAnswer(invocation -> counter.incrementAndGet());

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    velocityTracker.incrementAndExpire("vel:cust:concurrent:1m", 60);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(10L, counter.get());
    }
}
