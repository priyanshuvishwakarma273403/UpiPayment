# SentinelX — Real-Time Telemetry & Telemetry Architecture

## Overview
SentinelX processes real-time transaction telemetry with sub-10ms risk calculation SLAs across 27 microservices and a Python FastAPI inference engine.

## Telemetry Flow
1. **Gateway Ingress:** Spring Cloud Gateway receives payment requests.
2. **Kafka Event Broker:** Transaction events published to Kafka stream topics.
3. **Risk Scoring Engine:** Synchronous scoring (< 8.4 ms median latency).
4. **Fraud Graph & ML:** Scikit-learn / XGBoost model probability calculation.
5. **OpenTelemetry & Jaeger:** Distributed trace context propagated via headers (`traceparent`).
