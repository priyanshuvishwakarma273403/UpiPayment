package com.fraudService.observability.tracing;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

/**
 * OpenTelemetry W3C Distributed Tracing Filter & Loki Log Correlation Manager.
 * Propagates traceparent headers across Frontend -> Gateway -> Transaction -> Kafka -> Risk -> Fraud -> ML.
 */
@Component
@Slf4j
public class OpenTelemetryTraceContextFilter implements Filter {

    public static final String TRACEPARENT_HEADER = "traceparent";
    public static final String TRACESTATE_HEADER = "tracestate";
    public static final String TRACE_ID_KEY = "traceId";
    public static final String SPAN_ID_KEY = "spanId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String traceparent = httpRequest.getHeader(TRACEPARENT_HEADER);
        String traceId;
        String spanId;

        if (StringUtils.hasText(traceparent) && traceparent.startsWith("00-")) {
            String[] parts = traceparent.split("-");
            if (parts.length >= 4) {
                traceId = parts[1];
                spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            } else {
                traceId = generateHex32();
                spanId = generateHex16();
            }
        } else {
            traceId = generateHex32();
            spanId = generateHex16();
        }

        String outgoingTraceparent = String.format("00-%s-%s-01", traceId, spanId);

        // Bind to MDC for Loki log correlation
        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        httpResponse.setHeader(TRACEPARENT_HEADER, outgoingTraceparent);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
            MDC.remove(SPAN_ID_KEY);
        }
    }

    public static String generateHex32() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public static String generateHex16() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
