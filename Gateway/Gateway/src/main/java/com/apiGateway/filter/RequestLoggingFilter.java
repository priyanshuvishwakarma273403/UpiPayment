package com.apiGateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Instant start = Instant.now();

        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[{}] --> {} {} | IP: {} | User-Agent: {}",
                requestId,
                request.getMethod(),
                request.getPath().value(),
                getClientIp(request),
                request.getHeaders().getFirst("User-Agent"));

        // X-Request-Id header add karo (tracing ke liye)
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Request-Id", requestId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    long duration = Duration.between(start, Instant.now()).toMillis();

                    log.info("[{}] <-- {} {} | Status: {} | Time: {}ms",
                            requestId,
                            request.getMethod(),
                            request.getPath().value(),
                            response.getStatusCode(),
                            duration);
                }));
    }

    private String getClientIp(ServerHttpRequest request) {
        // X-Forwarded-For se real IP nikalo (behind proxy/load balancer)
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
