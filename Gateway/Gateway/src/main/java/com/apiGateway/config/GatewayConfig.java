package com.apiGateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * ================================================================
 * Gateway Configuration - Rate Limiting
 * ================================================================
 * Redis Rate Limiter: Token Bucket Algorithm use karta hai.
 *
 * replenishRate: Per second kitne tokens refill honge
 * burstCapacity: Bucket ki max capacity (spike handle karna)
 * requestedTokens: Ek request kitne tokens consume karegi
 *
 * KeyResolver:
 * - userKeyResolver: Logged-in user ke liye (X-User-Id header se)
 * - ipKeyResolver: Anonymous users ke liye (IP address se)
 *
 * Default: IP-based rate limiting
 * ================================================================
 */

@Configuration
public class GatewayConfig {

    /**
     * IP-based rate limiting (default - public/unauthenticated endpoints)
     * X-Forwarded-For header se real IP milta hai (behind proxy)
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver(){

        return exchange ->{
            String ip = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Forwarded-For");

            if(ip != null && !ip.isEmpty()){

                ip = ip.split(",")[0].trim();
            } else if (exchange.getRequest().getRemoteAddress() != null) {
                ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }else{
                ip = "unknown";
            }
            return Mono.just(ip);
        };

    }

    /**
     * User-based rate limiting (authenticated endpoints)
     * JWT filter ne X-User-Id header inject kiya hoga
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

    /**
     * Default Redis Rate Limiter Bean
     * application.yml mein per-route override bhi kar sakte hain
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // replenishRate=20, burstCapacity=40, requestedTokens=1
        return new RedisRateLimiter(20, 40, 1);
    }

}
