package com.fraudService.observability;

import com.fraudService.observability.config.PrometheusMetricsConfig;
import com.fraudService.observability.security.SecurityAuditLogger;
import com.fraudService.observability.tracing.OpenTelemetryTraceContextFilter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ObservabilitySecurityTest {

    private MeterRegistry meterRegistry;
    private PrometheusMetricsConfig metricsConfig;
    private SecurityAuditLogger auditLogger;
    private OpenTelemetryTraceContextFilter traceFilter;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsConfig = new PrometheusMetricsConfig();
        auditLogger = new SecurityAuditLogger();
        traceFilter = new OpenTelemetryTraceContextFilter();
    }

    @Test
    void testPrometheusMetricsRegistrationAndCounters() {
        Counter throughput = metricsConfig.transactionThroughputCounter(meterRegistry);
        Counter errors = metricsConfig.apiErrorsCounter(meterRegistry);
        Timer riskTimer = metricsConfig.riskEvaluationTimer(meterRegistry);

        assertNotNull(throughput);
        assertNotNull(errors);
        assertNotNull(riskTimer);

        throughput.increment(10.0);
        errors.increment(2.0);
        riskTimer.record(15, TimeUnit.MILLISECONDS);

        assertEquals(10.0, throughput.count());
        assertEquals(2.0, errors.count());
        assertEquals(1, riskTimer.count());
    }

    @Test
    void testOpenTelemetryTraceContextFilterPropagation() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader(OpenTelemetryTraceContextFilter.TRACEPARENT_HEADER))
                .thenReturn("00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");

        doAnswer(invocation -> {
            String traceId = MDC.get(OpenTelemetryTraceContextFilter.TRACE_ID_KEY);
            String spanId = MDC.get(OpenTelemetryTraceContextFilter.SPAN_ID_KEY);
            assertNotNull(traceId);
            assertNotNull(spanId);
            assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", traceId);
            return null;
        }).when(chain).doFilter(request, response);

        traceFilter.doFilter(request, response, chain);

        verify(response).setHeader(eq(OpenTelemetryTraceContextFilter.TRACEPARENT_HEADER), argThat(h -> h.startsWith("00-4bf92f3577b34da6a3ce929d0e0e4736-")));
    }

    @Test
    void testSecurityAuditLoggerSecretRedaction() {
        String rawInput = "user=john&password=SecretPassword123!&token=eyJhbGciOiJIUzI1Ni...&apiKey=my-api-key-999";
        String sanitized = auditLogger.sanitizeLog(rawInput);

        assertNotNull(sanitized);
        assertFalse(sanitized.contains("SecretPassword123!"));
        assertTrue(sanitized.contains("password=[REDACTED]"));
        assertTrue(sanitized.contains("token=[REDACTED]"));

        Map<String, Object> details = Map.of(
                "username", "john_doe",
                "password", "MySuperSecretPassword",
                "apiKey", "key-12345",
                "roles", "ROLE_USER"
        );

        Map<String, Object> cleanDetails = auditLogger.sanitizeMap(details);
        assertEquals("[REDACTED]", cleanDetails.get("password"));
        assertEquals("[REDACTED]", cleanDetails.get("apiKey"));
        assertEquals("john_doe", cleanDetails.get("username"));
    }
}
