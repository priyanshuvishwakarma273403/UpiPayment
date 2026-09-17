# SentinelX — Backend Microservice Contract Map

This document establishes the authoritative API contract mapping between the **SentinelX Frontend** and the 27 Spring Boot microservices plus 1 Python FastAPI ML service in the `d:\UpiMesh` backend repository.

---

## Architecture & Gateway Routing

- **Gateway Service URL:** `http://localhost:8080` (Spring Cloud Gateway)
- **FastAPI ML Service URL:** `http://localhost:8000`
- **Authentication Header:** `Authorization: Bearer <jwt_token>`
- **Content Type:** `application/json`

---

## 1. Auth Service (`com.authService`)

- **Base Route:** `/auth`
- **Frontend Pages:** `/login`, `/forgot-password`, `/reset-password`, `/onboarding`
- **Endpoints:**
  - `POST /auth/login` — Authenticate user credentials.
    - *Request:* `{ email, password }`
    - *Response:* `{ token, refreshToken, userId, role, name, upiId }`
  - `POST /auth/register` — Onboard new user / analyst.
    - *Request:* `{ name, email, password, role, upiId }`
    - *Response:* `{ token, userId, status }`
  - `POST /auth/refresh-token` — Rotate expired JWT token.
  - `POST /auth/logout` — Invalidate user session.
  - `POST /auth/forgot-password` — Request password reset OTP.
  - `POST /auth/reset-password` — Verify OTP and set new password.

---

## 2. Fraud Service (`com.fraudService`)

- **Base Route:** `/fraud`
- **Frontend Pages:** `/fraud`, `/fraud/[id]`, `/cases`, `/cases/[id]`, `/investigations/[id]`, `/ai-investigator`, `/mcp`, `/simulations`, `/network`, `/knowledge`
- **Controllers & Endpoints:**
  - **FraudController (`/fraud`)**:
    - `POST /fraud/check` — Check transaction fraud risk in real-time.
    - `GET /fraud/logs/{paymentId}` — Retrieve risk audit log for payment.
    - `GET /fraud/signals/{paymentId}` — Get extracted features & fraud signals.
    - `GET /fraud/high-risk` — List top high-risk flag alerts.
    - `GET /fraud/investigations` — Search all active fraud cases.
    - `POST /fraud/investigate/{paymentId}` — Manual analyst trigger to create investigation.
  - **FraudCaseController (`/fraud/cases`)**:
    - `GET /fraud/cases` — List fraud cases (filterable by status/severity).
    - `GET /fraud/cases/{id}` — Retrieve detailed investigation case profile.
    - `POST /fraud/cases/{id}/assign` — Assign case to analyst.
    - `PUT /fraud/cases/{id}/status` — Update status (`OPEN`, `INVESTIGATING`, `CONFIRMED_FRAUD`, `FALSE_POSITIVE`, `CLOSED`).
  - **FraudCopilotController (`/fraud/copilot`)**:
    - `POST /fraud/copilot/investigate` — Run multi-turn AI copilot diagnostic.
    - `GET /fraud/copilot/investigate/{entityId}` — Fetch past copilot investigation summary.
  - **FraudExplanationController (`/fraud/explain`)**:
    - `GET /fraud/explain/{paymentId}` — Fetch SHAP feature attribution breakdown.
  - **ContinuousLearningController (`/fraud/learning`)**:
    - `GET /fraud/learning/candidates` — Retraining candidate datasets.
    - `GET /fraud/learning/labels` / `POST /fraud/learning/labels` — Manage ground truth labels (`CONFIRMED_FRAUD`, `FALSE_POSITIVE`).
    - `POST /fraud/learning/train` — Trigger continuous model re-training job.
    - `GET /fraud/learning/history` — Retraining history & evaluation metrics.
    - `POST /fraud/learning/approve/{modelVersion}` — Human approval for candidate deployment.
  - **McpController (`/fraud/mcp`)**:
    - `GET /fraud/mcp/tools` — List registered Model Context Protocol tools.
    - `POST /fraud/mcp/execute` — Dispatch MCP tool execution.
    - `GET /fraud/mcp/audit` — Log of MCP executions.
  - **FraudNetworkController (`/fraud/network`)**:
    - `GET /fraud/network/clusters` — Retrieve graph network fraud rings.
    - `GET /fraud/network/transaction/{transactionId}` — Entity graph surrounding transaction.
    - `GET /fraud/network/customer/{customerId}` — Entity graph for customer nodes.
    - `GET /fraud/network/device/{deviceId}` — Shared device graph.
  - **ObservabilityController (`/fraud/observability`)**:
    - `GET /fraud/observability/metrics/summary` — Pipeline latency & throughput.
    - `GET /fraud/observability/tracing/active` — Active OpenTelemetry traces.
    - `GET /fraud/observability/security/audit` — Security & auth audit events.
  - **FraudPolicyRagController (`/fraud/policy`)**:
    - `POST /fraud/policy/query` — RAG query against compliance & fraud policy docs.
    - `POST /fraud/policy/search` — Search policy knowledge base.
    - `POST /fraud/policy/ingest` — Ingest new policy documentation.
  - **InvestigationSearchController (`/fraud/search`)**:
    - `GET /fraud/search` — Full-text search across cases, notes & transactions.
  - **FraudSimulatorController (`/fraud/simulator`)**:
    - `GET /fraud/simulator/presets` — Pre-packaged attack vectors (smurfing, account takeover, etc.).
    - `POST /fraud/simulator/run` — Run synthetic payment attack simulation.

---

## 3. Risk Service (`com.upimesh.risk`)

- **Base Route:** `/risk`
- **Frontend Pages:** `/risk`, `/customers`
- **Endpoints:**
  - `POST /risk/score` — Compute real-time risk score (0-1000).
  - `GET /risk/profile/{userId}` — Retrieve comprehensive user risk profile & tier.
  - `GET /risk/history/{userId}` — Historical risk score trajectory.
  - `GET /risk/rules` — List active rule engine configurations.

---

## 4. AML Service (`com.upimesh.aml`)

- **Base Route:** `/aml`
- **Frontend Pages:** `/aml`
- **Endpoints:**
  - `POST /aml/screen` — Screen entity against OFAC, PEP, and global watchlists.
  - `GET /aml/alerts/{userUpiId}` — Get AML alerts for UPI ID.
  - `PUT /aml/alert/{id}/resolve` — Resolve AML alert (`DISMISSED`, `SUSPICIOUS_ACTIVITY_REPORT_FILED`).
  - `GET /aml/watchlist` — Search AML watchlist entries.

---

## 5. Analytics Service (`com.upimesh.analytics`)

- **Base Route:** `/analytics`
- **Frontend Pages:** `/dashboard`, `/analytics`
- **Endpoints:**
  - `GET /analytics/dashboard` — Platform overview analytics (throughput, risk volume, approval rate).

---

## 6. AI Service (`com.aiService`)

- **Base Route:** `/ai`
- **Frontend Pages:** `/ai-investigator`, `/dashboard`
- **Endpoints:**
  - `POST /ai/chat` — Conversational assistant endpoint.
  - `POST /ai/fraud-explain` — Generative summary of fraud risk factors.
  - `POST /ai/expense-analysis` — Spending pattern anomaly detection.

---

## 7. Transaction Service (`com.transaction_service`)

- **Base Route:** `/` (Direct or via `/transaction`)
- **Frontend Pages:** `/transactions`, `/transactions/[id]`
- **Endpoints:**
  - `GET /` — List recent transactions.
  - `POST /` — Initiate new transaction.
  - `GET /user/{id}` — Get transactions by user ID.
  - `GET /merchant/{id}` — Get transactions by merchant ID.
  - `GET /spend-summary/{userId}` — Spend summary breakdown.

---

## 8. Payment Service (`com.paymentService`)

- **Base Route:** `/payment`
- **Frontend Pages:** `/payments`, `/transactions`
- **Endpoints:**
  - `POST /payment/pay` — Execute instant payment payload.
  - `GET /payment/{paymentId}` — Detailed payment execution status.
  - `POST /payment/verify` — Verify payment HMAC / checksum.
  - `GET /payment/history` — Payment audit ledger.
  - `POST /payment/offline-pay` — Offline transaction sync.

---

## 9. Wallet Service (`com.walletService`)

- **Base Route:** `/wallet`
- **Frontend Pages:** `/wallet`, `/customers`
- **Endpoints:**
  - `GET /wallet/balance/{userId}` — Get wallet balance.
  - `GET /wallet/info/{userId}` — Get wallet metadata & status.
  - `POST /wallet/add-money` — Top up wallet balance.
  - `POST /wallet/freeze` — Freeze wallet due to risk/fraud.
  - `POST /wallet/release` — Unfreeze wallet.
  - `POST /wallet/debit` / `POST /wallet/credit` — Execute wallet ledger entries.

---

## 10. Merchant Service (`com.merchantService`)

- **Base Route:** `/merchant`
- **Frontend Pages:** `/merchants`
- **Endpoints:**
  - `POST /merchant/register` — Onboard new merchant entity.
  - `GET /merchant/{id}` — Merchant profile & risk metadata.
  - `GET /merchant/qr/{merchantId}` — Static QR code asset.
  - `POST /merchant/qr/dynamic` — Generate dynamic payload QR.

---

## 11. KYC Service (`com.upimesh.kyc`)

- **Base Route:** `/api/v1/kyc`
- **Frontend Pages:** `/kyc`, `/customers`
- **Endpoints:**
  - `GET /api/v1/kyc/status/user/{userId}` — User KYC level & verification status.
  - `POST /api/v1/kyc/verify-pan` — Real-time PAN verification.
  - `POST /api/v1/kyc/verify-aadhaar` — Aadhaar OTP verification.
  - `POST /api/v1/kyc/face-match` — Liveness & face match verification score.
  - `GET /api/v1/kyc/audit-logs/{kycId}` — KYC decision audit trail.

---

## 12. Dispute Service (`com.upimesh.dispute`)

- **Base Route:** `/dispute`
- **Frontend Pages:** `/disputes`
- **Endpoints:**
  - `POST /dispute/raise` — File chargeback or dispute claim.
  - `GET /dispute/{disputeId}` — Dispute status and evidence trail.
  - `GET /dispute/user/{userUpiId}` — User disputes list.
  - `PUT /dispute/{disputeId}/status` — Resolve/escalate dispute.

---

## 13. Settlement Service (`com.upimesh.settlement`)

- **Base Route:** `/settlement`
- **Frontend Pages:** `/settlement`
- **Endpoints:**
  - `POST /settlement/run` — Trigger batch merchant settlement run.
  - `GET /settlement/batch/{batchId}` — Settlement batch status.
  - `GET /settlement/merchant/{merchantUpiId}` — Merchant settlement ledger.
  - `GET /settlement/batch/{batchId}/report` — Settlement export report.

---

## 14. Reconciliation Service (`com.upimesh.reconciliation`)

- **Base Route:** `/reconciliation`
- **Frontend Pages:** `/reconciliation`
- **Endpoints:**
  - `POST /reconciliation/run` — Run bank vs ledger reconciliation engine.
  - `GET /reconciliation/discrepancies/unresolved` — List mismatch discrepancies.
  - `PUT /reconciliation/discrepancy/{discrepancyId}/resolve` — Resolve discrepancy item.
  - `GET /reconciliation/report/{reportId}/summary` — Reconciliation report.

---

## 15. Invoice Service (`com.upimesh.invoice`)

- **Base Route:** `/invoice`
- **Frontend Pages:** `/merchants`, `/payments`
- **Endpoints:**
  - `POST /invoice/create` — Create B2B invoice.
  - `GET /invoice/{invoiceId}` — Retrieve invoice details.
  - `GET /invoice/{invoiceId}/pdf` — Export PDF invoice document.
  - `POST /invoice/{invoiceId}/mark-paid` — Update invoice state.

---

## 16. Python ML Service (`MlService/main.py`)

- **Base Route:** Direct FastAPI on `:8000` or proxied
- **Frontend Pages:** `/fraud`, `/simulations`, `/analytics`
- **Endpoints:**
  - `POST /predict` — Scikit-learn / XGBoost probability inference.
  - `POST /train` / `POST /train/continuous` — Train model pipeline.
  - `GET /metrics` — Precision, Recall, F1, ROC-AUC metrics.
  - `GET /models/history` / `GET /models/candidates` — Model registry.
  - `POST /models/approve` — Approve model version into production.

---

## 17. Additional Microservices

- **Loan Service (`/loan`):** Application, disbursement, repayment (`/loan/apply`, `/loan/{id}/disburse`).
- **Payroll Service (`/payroll`):** Batch payroll processing (`/payroll/batch/create`, `/payroll/batch/{id}/process`).
- **Subscription Service (`/subscription`):** Recurring UPI mandates (`/subscription/create`, `/subscription/{subscriptionId}/cancel`).
- **Referral Service (`/referral`):** Referral tracking & credit (`/referral/stats/{userId}`).
- **Rewards Service (`/rewards`):** Cashbacks & loyalty (`/balance/{userId}`, `/redeem`).
- **NPCI Service (`/npci`):** Direct NPCI gateway simulation (`/npci/initiate-transaction`, `/npci/refund`).
- **BankGateway Service (`/bank`):** Core banking integration fallback (`/bank/transfer`, `/bank/verify-account`).
- **Notification Service (`/notification`):** Transaction alerts & SMS/Email delivery (`/status`).
- **Sync Service (`/sync`):** Offline transaction synchronization engine (`/sync/process`).

---

## Frontend Integration Standard

All pages in `src/app/**` utilize `src/lib/api.ts` which proxies calls through the Central API Client. When a backend service is unavailable or returning 503/404, the frontend displays an **Honest Unavailable State** with options to retry, inspect backend contract status, or review documentation.
