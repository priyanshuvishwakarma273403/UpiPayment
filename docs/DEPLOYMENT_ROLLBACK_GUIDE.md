# SentinelX UPI Mesh — Production Deployment & Rollback Operational Guide

## 1. Overview
This operational guide details zero-downtime deployment, health verification, rollback procedures, and failure recovery protocols for **SentinelX / UPI Mesh**.

---

## 2. Production Deployment Workflow

### Pre-requisites:
- Kubernetes Cluster (`kubectl` context configured)
- Container Registry images built & scanned via GitHub Actions CI/CD (`ci.yml`, `security-scan.yml`)

### Step 1: Apply Namespace, ConfigMaps & Secrets
```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
```

### Step 2: Apply Service Deployments & Ingress
```bash
kubectl apply -f k8s/gateway-deployment.yaml
kubectl apply -f k8s/transaction-deployment.yaml
kubectl apply -f k8s/fraud-deployment.yaml
kubectl apply -f k8s/ml-service-deployment.yaml
kubectl apply -f k8s/ingress.yaml
```

---

## 3. Health Verification & Validation

### Verify Rollout Status:
```bash
kubectl rollout status deployment/gateway-service -n sentinelx-prod
kubectl rollout status deployment/transaction-service -n sentinelx-prod
kubectl rollout status deployment/fraud-service -n sentinelx-prod
kubectl rollout status deployment/ml-service -n sentinelx-prod
```

### Check Actuator & Service Health Endpoints:
```bash
# Fraud Service Actuator Health
curl -s http://localhost:8086/actuator/health | jq .

# ML Service Health
curl -s http://localhost:8000/health | jq .
```

---

## 4. Rollback Procedures

If a newly deployed image exhibits runtime anomalies, elevated API error rates (HTTP 5xx), or readiness probe failures:

### Step 1: Inspect Rollout History
```bash
kubectl rollout history deployment/fraud-service -n sentinelx-prod
```

### Step 2: Immediate One-Command Rollback to Previous Version
```bash
kubectl rollout undo deployment/fraud-service -n sentinelx-prod
```

### Step 3: Rollback to Specific Revision
```bash
kubectl rollout undo deployment/fraud-service -n sentinelx-prod --to-revision=3
```

---

## 5. Failure Recovery & Disaster Protocol

| Failure Scenario | Automated Safeguard | Operator Incident Action |
|---|---|---|
| **CrashLoopBackOff / Startup Failure** | Liveness Probe restarts pod after 3 retries | Run `kubectl logs -n sentinelx-prod -l app=fraud-service --previous` to inspect failure root cause. Execute `kubectl rollout undo`. |
| **High Traffic Spike / Memory Pressure** | HorizontalPodAutoscaler (HPA) auto-scales replicas (min 2, max 8) | Monitor via Prometheus `/fraud/observability/metrics/summary`. Temporarily increase HPA maxReplicas if needed. |
| **Database Connection Outage** | Spring Boot HikariCP connection retry pool & circuit breakers | Verify DB pod state. Check secret credentials. Restart app pods post-recovery using `kubectl rollout restart deployment/fraud-service`. |
