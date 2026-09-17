# UPI Mesh & SentinelX — Comprehensive Services Architecture Catalog

> **Author:** 15+ Years Principal Fintech & Distributed Systems Architect  
> **Classification:** Production Architecture & Integration Reference  
> **Scope:** Full Topology of all 27 Microservices, Data Tiers, and Communication Fabrics  

---

## 1. Complete Service Topology Matrix

| # | Service Name | Directory | Default Port | Eureka ID | Primary Backing Store | Technology Stack |
|:---:|---|---|:---:|---|---|---|
| 1 | **Eureka** | `Eureka/Eureka` | `8761` | `EUREKA-SERVER` | In-Memory Registry | Spring Boot, Spring Cloud Netflix Eureka |
| 2 | **Gateway** | `Gateway/Gateway` | `8080` | `API-GATEWAY` | Reactive Redis | Spring Boot, Spring Cloud Gateway (WebFlux), Resilience4j |
| 3 | **Auth** | `Auth/Auth` | `8081` | `AUTH-SERVICE` | MySQL (`upi_auth_db`) + Redis | Spring Boot, Spring Security, JJWT, BCrypt |
| 4 | **Wallet** | `Wallet/Wallet` | `8082` | `WALLET-SERVICE` | MySQL (`upi_wallet_db`) + Redis | Spring Boot, JPA, Spring Kafka |
| 5 | **Payment** | `Payment/Payment` | `8083` | `PAYMENT-SERVICE` | MySQL (`upi_payment_db`) | Spring Boot, JPA, Spring Kafka, OpenFeign, RSA |
| 6 | **Transaction** | `Transaction/Transaction`| `8084` | `TRANSACTION-SERVICE`| MySQL (`upi_transaction_db`) + Mongo | Spring Boot, JPA, Spring Data MongoDB, Spring Kafka |
| 7 | **Merchant** | `Merchant/Merchant` | `8085` | `MERCHANT-SERVICE` | MySQL (`upi_merchant_db`) | Spring Boot, JPA, ZXing (QR) |
| 8 | **Fraud** | `Fraud/Fraud` | `8086` | `FRAUD-SERVICE` | In-Memory / Mongo (`upi_fraud_db`) | Spring Boot, Redis, Spring Kafka |
| 9 | **AiService** | `AiService/AiService` | `8087` | `AI-SERVICE` | MongoDB (`upi_ai_db`) | Spring Boot, Spring AI, OpenAI ChatClient |
| 10 | **Notification**| `Notification/Notification`| `8088`| `NOTIFICATION-SERVICE`| In-Memory / SMTP | Spring Boot, Spring Kafka, Spring Mail, Thymeleaf |
| 11 | **Sync** | `Sync/Sync` | `8089` | `SYNC-SERVICE` | MySQL (`upi_sync_db`) | Spring Boot, JPA, Spring Kafka, OpenFeign |
| 12 | **NPCI** | `NPCI/NPCI` | `8090` | `NPCI-INTEGRATION-SERVICE`| MySQL (`npci_db`) + Redis | Spring Boot, JPA, OpenFeign, AES-256-GCM |
| 13 | **BankGateway** | `BankGateway/BankGateway`| `8091` | `BANK-GATEWAY-SERVICE` | MySQL (`bank_gateway_db`) + Redis | Spring Boot, JPA, OpenFeign, Bank Strategy Pattern |
| 14 | **Settlement** | `Settlement/Settlement` | `8092` | `SETTLEMENT-SERVICE` | MySQL (`settlement_db`) | Spring Boot, JPA, Quartz Scheduler, OpenFeign |
| 15 | **Reconciliation**| `Reconciliation/Reconciliation`| `8093`| `RECONCILIATION-SERVICE`| MySQL (`reconciliation_db`)| Spring Boot, JPA, Apache POI (Excel), Quartz |
| 16 | **Kyc** | `Kyc/Kyc` | `8094` | `KYC-SERVICE` | MySQL (`kyc_db`) + Redis | Spring Boot, JPA, AES-256-GCM, UIDAI/NSDL Sim |
| 17 | **Aml** | `Aml/Aml` | `8095` | `AML-SERVICE` | MySQL (`aml_db`) + Redis | Spring Boot, JPA, Levenshtein DP, Redis Counters |
| 18 | **Risk** | `Risk/Risk` | `8096` | `RISK-SCORING-SERVICE`| MySQL (`risk_db`) + Redis | Spring Boot, JPA, Rule Engine Strategy Pattern |
| 19 | **Subscription**| `Subscription/Subscription`| `8098`| `SUBSCRIPTION-SERVICE`| MySQL (`subscription_db`) | Spring Boot, JPA, Quartz Scheduler, OpenFeign |
| 20 | **Invoice** | `Invoice/Invoice` | `8099` | `INVOICE-SERVICE` | MySQL (`invoice_db`) + Redis | Spring Boot, JPA, iText7 PDF Generation |
| 21 | **Dispute** | `Dispute/Dispute` | `8100` | `DISPUTE-SERVICE` | MySQL (`dispute_db`) + Redis | Spring Boot, JPA, OpenFeign |
| 22 | **Rewards** | `Rewards/Rewards` | `8101` | `REWARDS-SERVICE` | MySQL (`rewards_db`) + Redis | Spring Boot, JPA, Loyalty Points Ledger |
| 23 | **Loan** | `Loan/Loan` | `8102` | `LOAN-SERVICE` | MySQL (`loan_db`) | Spring Boot, JPA, Amortization Engine, OpenFeign |
| 24 | **Referral** | `Referral/Referral` | `8103` | `REFERRAL-SERVICE` | MySQL (`referral_db`) + Redis | Spring Boot, JPA, OpenFeign, Device Fingerprinting |
| 25 | **Payroll** | `Payroll/Payroll` | `8104` | `PAYROLL-SERVICE` | MySQL (`payroll_db`) | Spring Boot, JPA, OpenFeign, iText7 Salary Slips |
| 26 | **Analytics** | `Analytics/Analytics` | `8105` | `ANALYTICS-SERVICE` | MySQL (`analytics_db`) + Redis | Spring Boot, JPA, OpenFeign, Metric Aggregation |
| 27 | **MlService** | `MlService/` | `8000` | Standalone FastAPI | File / Memory / MLflow | Python 3.11, FastAPI, Scikit-Learn, Pandas |

---

## 2. Granular Microservice Specifications

### 1. Eureka (`eureka-server` — Port 8761)
- **Role:** Central dynamic discovery registry maintaining live heartbeats and host addresses for all microservices.
- **Security:** HTTP Basic Authentication (`admin` / protected password).
- **Core Endpoints:**
  - `GET /eureka/apps` — Registry instance list.
  - `GET /` — Interactive web dashboard for monitoring cluster node health.

---

### 2. Gateway (`api-gateway` — Port 8080)
- **Role:** Non-blocking reactive reverse proxy, TLS termination, CORS policy management, JWT verification, and Redis-backed token bucket rate limiting.
- **Filters:**
  - `JwtAuthenticationFilter`: Validates JWT Bearer tokens, extracts claims, strips untrusted client headers, and forwards sanitized `X-User-Id`, `X-User-Email`, `X-User-Roles` to downstream services.
  - `Resilience4jCircuitBreaker`: Fails open with graceful fallbacks during cascading downstream outages.
  - `RequestRateLimiter`: Reactive Redis token-bucket per IP/User handle.

---

### 3. Auth Service (`auth-service` — Port 8081)
- **Role:** User identity, phone/email signup, OTP delivery and validation via Redis, BCrypt password hashing, and JWT issuance with refresh token rotation.
- **Database:** MySQL (`upi_auth_db`) — Tables: `users`, `roles`, `refresh_tokens`.
- **Key Endpoints:**
  - `POST /auth/register` — Onboards customer, validates uniqueness, and issues `<phone>@upimesh` VPA handle.
  - `POST /auth/login` — Verifies credentials, returns short-lived JWT (15 min) and long-lived Refresh Token (7 days).
  - `POST /auth/verify-otp` — Validates 6-digit OTP cached in Redis.
  - `POST /auth/refresh-token` — Rotates refresh token in MySQL and issues fresh access token.
  - `GET /auth/users/{id}` — Internal endpoint for user demographic lookup.

---

### 4. Wallet Service (`wallet-service` — Port 8082)
- **Role:** Digital balance authority, deposits, withdrawals, and real-time ledger debit/credit operations.
- **Database:** MySQL (`upi_wallet_db`) — Tables: `wallets`, `wallet_transactions`.
- **Key Endpoints:**
  - `GET /wallet/balance/{userId}` — Cached balance lookup via Redis.
  - `POST /wallet/create` — Initializes wallet ledger account upon registration.
  - `POST /wallet/add-money` — Simulates top-up via external banking rails.
  - `POST /wallet/debit` & `POST /wallet/credit` — Internal transactional balance mutations.
- **Event Bus:**
  - Consumes: `payment_initiated` (executes balance debit/credit).
  - Publishes: `payment_completed`, `payment_failed`.

---

### 5. Payment Service (`payment-service` — Port 8083)
- **Role:** Core payment transaction orchestrator. Enforces idempotency, creates RSA digital signatures for non-repudiation, validates wallet balance via OpenFeign, and initiates event-driven settlement.
- **Database:** MySQL (`upi_payment_db`) — Table: `payments`.
- **Key Endpoints:**
  - `POST /payment/pay` — Synchronous online payment initiation.
  - `POST /payment/offline-pay` — Queues RSA-signed offline mesh transactions.
  - `POST /payment/verify` — Validates RSA digital signature against stored public key.
  - `GET /payment/{paymentId}` — Fetches transaction status and payment lifecycle metadata.
- **Outbound Feign:**
  - `WalletServiceClient` (`wallet-service`): Balance inquiries and direct debit/credit.
- **Event Bus:**
  - Publishes: `payment_initiated`, `payment_completed`, `payment_failed`.
  - Consumes: `fraud_detected`.

---

### 6. Transaction Service (`transaction-service` — Port 8084)
- **Role:** Polyglot immutable transaction journal, merchant transaction aggregation, and customer spending summaries.
- **Databases:**
  - MySQL (`upi_transaction_db`): Relational ledger table `transactions`.
  - MongoDB (`upi_transaction_audit_db`): Unstructured raw audit collections `audit_logs` and `payment_logs`.
- **Key Endpoints:**
  - `GET /transactions/user/{id}` — Paginated transaction history for mobile clients.
  - `GET /transactions/merchant/{id}` — Merchant transaction stream.
  - `GET /transactions/spend-summary/{userId}` — Monthly and categorical spending summaries.
- **Event Bus:**
  - Consumes: `payment_completed`, `payment_failed`, `sync_completed`.

---

### 7. Merchant Service (`merchant-service` — Port 8085)
- **Role:** Merchant business verification, store profile onboarding, static QR code generation (Base64 PNG), and dynamic amount-tagged QR generation via ZXing.
- **Database:** MySQL (`upi_merchant_db`) — Tables: `merchants`, `merchant_qr_codes`.
- **Key Endpoints:**
  - `POST /merchant/register` — Onboards merchant business account and assigns merchant UPI handle.
  - `GET /merchant/qr/{merchantId}` — Generates static BharatQR/UPI QR code.
  - `POST /merchant/qr/dynamic` — Generates dynamic invoice QR code encoded with transaction amount and reference.

---

### 8. Fraud Service (`fraud-service` — Port 8086)
- **Role:** Real-time rule-based screening engine intercepting velocity spikes, duplicate payment amounts, and unusual night-time hours (11 PM - 5 AM).
- **Storage:** In-Memory cache and MongoDB (`upi_fraud_db`).
- **Key Endpoints:**
  - `POST /fraud/check` — Synchronous evaluation returning `SAFE`, `SUSPICIOUS`, or `BLOCKED`.
  - `GET /fraud/logs/{paymentId}` — Detailed reason audit log for flagged transactions.
  - `GET /fraud/stats` — Fraud engine operational telemetry.

---

### 9. AI Service (`ai-service` — Port 8087)
- **Role:** Natural Language conversational assistant, financial expenditure analyzer, and explainable AI generating human-readable explanations for fraud blocks.
- **Database:** MongoDB (`upi_ai_db`) — Collection: `chat_history`.
- **Technology:** Spring AI with OpenAI ChatClient integration.
- **Key Endpoints:**
  - `POST /ai/chat` — Conversational assistant with context memory.
  - `POST /ai/expense-analysis` — Categorizes spending and highlights budgeting anomalies.
  - `POST /ai/fraud-explain` — Translates risk engine rule codes into user-friendly security advisories.

---

### 10. Notification Service (`notification-service` — Port 8088)
- **Role:** Asynchronous notification dispatcher consuming Kafka events to render HTML email templates (Thymeleaf) and dispatch SMS alerts.
- **Outbound Integrations:** SMTP / SendGrid and mock SMS Gateway.
- **Event Bus:**
  - Consumes: `payment_completed`, `payment_failed`, `sync_completed`.

---

### 11. Sync Service (`sync-service` — Port 8089)
- **Role:** Offline mesh synchronization coordinator. When offline clients reconnect to cell networks or Wi-Fi, this service batches pending signed transactions, validates nonces, and initiates settlement.
- **Database:** MySQL (`upi_sync_db`) — Table: `sync_records`.
- **Outbound Feign:**
  - `PaymentServiceClient` (`payment-service`): Ingests verified offline transactions.
- **Event Bus:**
  - Publishes: `sync_completed`.

---

### 12. NPCI Switch Service (`npci-integration-service` — Port 8090)
- **Role:** Simulates the National Payments Corporation of India (NPCI) central switch. Handles inter-bank routing, ISO-8583 response codes, mandate lifecycle management, and payload encryption.
- **Database:** MySQL (`npci_db`) — Tables: `upi_transactions`, `upi_mandates`, `refund_records`.
- **Key Endpoints:**
  - `POST /npci/initiate-transaction` — Central switch transaction routing.
  - `GET /npci/check-status/{transactionId}` — Status verification.
  - `POST /npci/refund` — Processes transaction chargeback and refunds.
  - `POST /npci/mandate-create` — Registers e-mandate for recurring AutoPay.

---

### 13. Bank Gateway Service (`bank-gateway-service` — Port 8091)
- **Role:** Core banking abstraction layer utilizing the **Strategy Pattern** to route requests to specific commercial bank implementations (HDFC, SBI, ICICI, Axis, Kotak). Handles balance inquiry, account linking, and IFSC lookup.
- **Database:** MySQL (`bank_gateway_db`) — Tables: `linked_bank_accounts`, `ifsc_details`, `upi_handle_resolutions`.
- **Key Endpoints:**
  - `POST /bank/accounts/link` — Links customer bank account using debit card verification.
  - `POST /bank/accounts/balance` — Direct balance query to issuing bank core banking system (CBS).
  - `GET /bank/ifsc/{ifscCode}` — Resolves branch name, RTGS/NEFT eligibility from IFSC.
  - `GET /bank/upi/resolve/{upiHandle}` — Maps VPA to destination IFSC and account number.

---

### 14. Settlement Service (`settlement-service` — Port 8092)
- **Role:** End-of-Day (EOD) automated batch settlement engine. Aggregates unsettled merchant transactions, computes platform commissions (0.2%) and GST (18%), and dispatches bulk payouts via BankGateway.
- **Database:** MySQL (`settlement_db`) — Tables: `settlement_batches`, `merchant_settlements`, `settlement_transactions`.
- **Scheduler:** Quartz Scheduler configured for midnight batch runs (`0 0 0 * * ?`).
- **Key Endpoints:**
  - `POST /settlement/run` — Manually triggers daily settlement batch.
  - `GET /settlement/batch/{batchId}` — Fetches batch audit details.
  - `GET /settlement/merchant/{merchantUpiId}` — Merchant historical payout records.

---

### 15. Reconciliation Service (`reconciliation-service` — Port 8093)
- **Role:** Daily ledger reconciliation engine. Compares internal transaction logs against external bank statement feeds and NPCI reports, identifying discrepancies and generating audit-ready Excel reports.
- **Database:** MySQL (`reconciliation_db`) — Tables: `reconciliation_reports`, `reconciliation_discrepancies`.
- **Key Endpoints:**
  - `POST /reconciliation/run` — Executes 3-way reconciliation audit for a target date.
  - `GET /reconciliation/report/{reportId}/export` — Streams generated `.xlsx` workbook formatted via Apache POI.
  - `PUT /reconciliation/discrepancy/{discrepancyId}/resolve` — Allows compliance officers to clear audited items.

---

### 16. KYC Service (`kyc-service` — Port 8094)
- **Role:** Regulatory customer identity verification aligning with RBI Master Directions on PPIs (Prepaid Payment Instruments). Manages Tier 0, Tier 1, and Tier 2 KYC verification using AES-256-GCM encryption.
- **Database:** MySQL (`kyc_db`) — Tables: `kyc_records`, `kyc_documents`, `kyc_audit_logs`.
- **Key Endpoints:**
  - `POST /api/v1/kyc/initiate-aadhaar` — Dispatches simulated UIDAI OTP.
  - `POST /api/v1/kyc/verify-aadhaar` — Verifies OTP and encrypts Aadhaar number with AES-256-GCM.
  - `POST /api/v1/kyc/verify-pan` — Validates PAN with simulated NSDL database (upgrades limit to ₹1,00,000/mo).
  - `POST /api/v1/kyc/face-match` — Compares selfie against document photo (unlocks unlimited limit).

---

### 17. AML Service (`aml-service` — Port 8095)
- **Role:** Compliance screening for PMLA 2002 guidelines. Enforces sliding-window transaction velocity, 24-hour smurfing/structuring detection, and Levenshtein fuzzy matching against sanctions and PEP lists.
- **Database:** MySQL (`aml_db`) — Tables: `aml_alerts`, `aml_screening_results`, `watchlist_entries`.
- **Key Endpoints:**
  - `POST /aml/screen` — Screens a live transaction for money laundering indicators.
  - `GET /aml/alerts/{userUpiId}` — Queries active AML flags.
  - `PUT /aml/alert/{alertId}/escalate` — Escalates suspicious case to compliance director.

---

### 18. Risk Scoring Service (`risk-scoring-service` — Port 8096)
- **Role:** Real-time multi-signal behavioral risk evaluation engine executing 9 modular rule strategies to output composite risk scores (0-100) and step-up authentication recommendations.
- **Database:** MySQL (`risk_db`) — Tables: `risk_profiles`, `risk_scoring_results`.
- **Key Endpoints:**
  - `POST /risk/score` — Real-time transaction scoring.
  - `GET /risk/profile/{userId}` — Customer behavioral risk profile and rolling statistics.

---

### 19. Subscription Service (`subscription-service` — Port 8098)
- **Role:** Manages recurring subscription mandates (UPI AutoPay), scheduling automatic charges via Quartz, executing payments via NPCI, and enforcing the 3-step RBI dunning cadence.
- **Database:** MySQL (`subscription_db`) — Tables: `subscriptions`, `subscription_payment_attempts`.
- **Key Endpoints:**
  - `POST /subscription/create` — Establishes recurring mandate.
  - `PUT /subscription/{subscriptionId}/pause` & `resume` — Customer mandate controls.
  - `POST /subscription/execute-scheduled` — Scheduled batch charge trigger.

---

### 20. Invoice Service (`invoice-service` — Port 8099)
- **Role:** B2B merchant invoicing platform calculating GST (CGST/SGST/IGST), discount structures, generating dynamic payment QR codes, and streaming PDF tax invoices via iText7.
- **Database:** MySQL (`invoice_db`) — Tables: `invoices`, `invoice_line_items`.
- **Key Endpoints:**
  - `POST /invoice/create` — Generates itemized tax invoice.
  - `GET /invoice/{invoiceId}/pdf` — Streams compiled PDF invoice document.

---

### 21. Dispute Service (`dispute-service` — Port 8100)
- **Role:** End-to-end payment dispute lifecycle management. Enforces 90-day dispute eligibility, 48-hour merchant rebuttal SLA, evidence attachment, and automated arbitration via NPCI switch.
- **Database:** MySQL (`dispute_db`) — Tables: `disputes`, `dispute_evidences`.
- **Key Endpoints:**
  - `POST /dispute/raise` — Files formal transaction dispute.
  - `POST /dispute/{disputeId}/merchant-response` — Uploads merchant rebuttal and proof of delivery.
  - `POST /dispute/{disputeId}/resolve` — Arbitrates claim and triggers refund if upheld.

---

### 22. Rewards Service (`rewards-service` — Port 8101)
- **Role:** User loyalty program, cashback campaigns, point accrual, and reward redemption ledger.
- **Database:** MySQL (`rewards_db`) — Tables: `offers`, `reward_ledger`, `user_reward_accounts`.
- **Key Endpoints:**
  - `POST /rewards/credit` — Credits promotional or referral points.
  - `POST /rewards/redeem` — Converts accumulated points into wallet credits.
  - `GET /rewards/balance/{userId}` — Queries available reward points.

---

### 23. Loan Service (`loan-service` — Port 8102)
- **Role:** Digital micro-lending and Buy Now Pay Later (BNPL). Simulates CIBIL credit scoring, computes reducing-balance EMI schedules, disburses loan funds, and collects repayments.
- **Database:** MySQL (`loan_db`) — Tables: `loan_applications`, `loan_repayments`.
- **Key Endpoints:**
  - `POST /loan/apply` — Submits credit line application.
  - `POST /loan/{loanId}/disburse` — Transfers approved funds to customer wallet.
  - `GET /loan/{loanId}/schedule` — Returns month-by-month principal and interest amortization table.
  - `POST /loan/{loanId}/repay` — Ingests EMI repayment.

---

### 24. Referral Service (`referral-service` — Port 8103)
- **Role:** Customer growth referral engine generating unique referral codes, tracking conversion funnels, and validating device fingerprints in Redis to eliminate synthetic referral fraud.
- **Database:** MySQL (`referral_db`) — Tables: `referral_codes`, `referral_records`.
- **Key Endpoints:**
  - `POST /referral/code/generate` — Issues shareable referral handle.
  - `POST /referral/track` — Logs referee installation and sign-up.
  - `POST /referral/qualify` — Triggers rewards upon referee's first successful transaction.

---

### 25. Payroll Service (`payroll-service` — Port 8104)
- **Role:** Corporate bulk salary disbursement engine. Calculates statutory deductions (PF, ESI, TDS), verifies employee bank accounts via BankGateway, executes transfers via NPCI, and generates PDF salary slips.
- **Database:** MySQL (`payroll_db`) — Tables: `payroll_batches`, `employee_payments`.
- **Key Endpoints:**
  - `POST /payroll/batch/create` — Uploads and creates payroll batch.
  - `POST /payroll/batch/{id}/process` — Executes bulk salary distribution.
  - `GET /payroll/slip/{paymentId}/pdf` — Downloads itemized employee pay slip.

---

### 26. Analytics Service (`analytics-service` — Port 8105)
- **Role:** Business intelligence aggregator computing hourly and daily transaction volumes, platform Gross Merchandise Value (GMV), active user growth, and gateway throughput.
- **Database:** MySQL (`analytics_db`) — Tables: `daily_metrics`, `hourly_metrics`.
- **Key Endpoints:**
  - `GET /analytics/dashboard/summary` — High-level platform KPIs.
  - `GET /analytics/transactions/volume` — Time-series transaction volume metrics.

---

### 27. SentinelX ML Service (`ml-service` — Port 8000)
- **Role:** High-performance Python machine learning service executing real-time fraud probability inference, continuous model retraining, MLflow experiment tracking, and controlled human model promotion gates.
- **Framework:** FastAPI, Scikit-Learn (Random Forest), Pandas, NumPy, MLflow Tracker.
- **Key Endpoints:**
  - `POST /predict` — Evaluates 8-dimensional feature vector; returns fraud probability and confidence.
  - `POST /train/continuous` — Ingests investigator-labeled feedback to train candidate models.
  - `GET /models/candidates` — Lists candidate models awaiting approval.
  - `POST /models/approve` — Human-in-the-loop promotion gate deploying model to production.
  - `GET /metrics` — Precision, Recall, F1, ROC-AUC, PR-AUC of active production model.

---

*Authored by Principal Fintech Systems Architect for the UPI Mesh Platform.*
