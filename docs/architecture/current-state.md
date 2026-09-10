# UPI MESH / SENTINELX — CURRENT STATE ARCHITECTURE AUDIT

> **Audit Timestamp:** September 2026  
> **Repository:** `d:\UpiMesh`  
> **Phase:** 01 — Existing System Audit & Architecture Recovery  
> **Audit Principle:** Separate **CURRENT** from **PLANNED**. Do not describe future architecture as existing functionality. Document only verified facts.

---

## 1. Executive Summary

A comprehensive architectural and codebase audit was performed across all **26 microservices** present in the `UpiMesh` repository. The codebase contains significant, high-value domain engineering work representing a modern fintech ecosystem—including UPI 2.0 payment processing, offline cryptographic synchronization, multi-tier KYC (Aadhaar/PAN/Face Match), anti-money laundering (PMLA 2002), fraud screening, batch payroll disbursement, merchant QR code issuance, automated settlements, and bank reconciliation.

However, the audit revealed critical architectural disconnections, contract mismatches, and build failures resulting from two distinct development eras in the repository:
1. **The Core Platform Services (`com.<serviceName>`):** `Auth`, `Eureka`, `Fraud`, `Gateway`, `Merchant`, `Notification`, `Payment`, `Sync`, `Transaction`, and `Wallet`. These services rely on Kafka and Redis for event-driven payment processing, but suffer from zero unit tests (only `contextLoads()` smoke tests), hardcoded bypasses (e.g., fraud check is hardcoded to `SAFE`), missing test dependencies in POMs, and syntax errors.
2. **The Extended Fintech Ecosystem (`com.upimesh.<serviceName>` & `com.npci`):** `Aml`, `BankGateway`, `Dispute`, `Invoice`, `Kyc`, `Loan`, `Payroll`, `Reconciliation`, `Referral`, `Rewards`, `Risk`, `Settlement`, and `Subscription`. These services possess rich domain logic and extensive Mockito unit tests, but are almost completely disconnected from the API Gateway (14 services have no Gateway route), lack inter-service caller integration (leaving `Aml`, `Risk`, `Kyc`, and `Invoice` orphaned), have broken compilation or test runs in 7 services, and suffer from inter-service REST contract mismatches.

---

## 2. Current Architecture (CURRENT)

### 2.1 Service Topology
The ecosystem is structured around Netflix Eureka for service registry (`eureka-server` on port `8761`) and a reactive Spring Cloud Gateway (`api-gateway` on port `8080`).

```
                              [ React Client / External Callers ]
                                              │
                                              ▼
                                 [ API Gateway - Port 8080 ]
                                 (Reactive WebFlux / Redis)
                                              │
                     ┌────────────────────────┼────────────────────────┐
                     ▼ (Route: /auth/**)      ▼ (Route: /payment/**)   ▼ (Route: /wallet/**)
             [ Auth Service ]         [ Payment Service ]       [ Wallet Service ]
                Port 8081                 Port 8083                 Port 8082
             (MySQL + Redis)           (MySQL + Kafka)           (MySQL + Kafka)
                     │                        │                         │
                     │                        ▼ (Kafka: payment_        │
                     │                           initiated)             │
                     │                        └────────────┬────────────┘
                     │                                     ▼
                     │                            [ Kafka Broker: 9092 ]
                     │                                     │
                     │                                     ▼ (Kafka: payment_completed / failed)
                     │                        ┌────────────┴────────────┐
                     │                        ▼                         ▼
                     │              [ Transaction Service ]   [ Notification Service ]
                     │                    Port 8084                 Port 8088
                     │               (MySQL + MongoDB)            (SMTP / SMS)
                     │                        │
                     └────────────────────────┘ (RestTemplate: /auth/users)
```

### 2.2 Dual Routing Reality
While the repository documentation asserts that all services are routed through the central Gateway, verification of `Gateway/src/main/resources/application.yml` confirms that:
- **Only 10 services are routed:** `Auth (8081)`, `Wallet (8082)`, `Payment (8083)`, `Transaction (8084)`, `Merchant (8085)`, `Fraud (8086)`, `AiService (8087)`, `Sync (8089)`, `Payroll (8104)`, `Analytics (8105)`.
- **14 services have NO route configured:** `BankGateway (8091)`, `Settlement (8092)`, `Reconciliation (8093)`, `Kyc (8094)`, `Aml (8095)`, `Risk (8096)`, `Subscription (8098)`, `Invoice (8099)`, `Dispute (8100)`, `Rewards (8101)`, `Loan (8102)`, `Referral (8103)`, `NPCI (8090)`, and `Notification (8088)`.

---

## 3. Service Map

| Service Name | Port | Primary Responsibility | Backing Storage | Current Operational Status |
|---|---|---|---|---|
| **Eureka** | `8761` | Service Registry & Discovery | In-Memory | Fully Functional |
| **Gateway** | `8080` | Central Edge Routing & Rate Limiting | Redis | Test Build Broken (Missing test dep) |
| **Auth** | `8081` | Authentication & Redis OTP | MySQL (`upi_auth_db`) + Redis | Test Build Broken (Missing test dep) |
| **Wallet** | `8082` | Balance Ledger & Credit/Debit | MySQL (`upi_wallet_db`) + Redis | Functional (Zero unit tests) |
| **Payment** | `8083` | Payment Orchestration & Kafka Dispatch | MySQL (`upi_payment_db`) | Functional (Zero unit tests; Fraud bypassed) |
| **Transaction** | `8084` | Transaction Ledger & Audit Logs | MySQL (`upi_transaction_db`) + Mongo | **BROKEN BUILD** (Syntax error in Repo) |
| **Merchant** | `8085` | Merchant Onboarding & QR Generation | MySQL (`upi_merchant_db`) | Functional (Zero unit tests) |
| **Fraud** | `8086` | Rule-Based Fraud Detection | In-Memory / Mongo (`upi_fraud_db`) | Functional (Disconnected from payments) |
| **AiService** | `8087` | LLM Chat & Spending Analysis | MongoDB (`upi_ai_db`) | Test Build Broken; Hardcoded mock data |
| **Sync** | `8089` | Offline Payment Reconciliation | MySQL (`upi_sync_db`) | Functional |
| **Notification** | `8088` | Email & SMS Dispatch | In-Memory / Kafka | Functional |
| **NPCI** | `8090` | UPI Switch & Mandate Processing | MySQL (`npci_db`) + Redis | **BROKEN BUILD** (Missing symbols/classes) |
| **BankGateway** | `8091` | Core Bank Abstraction & VPA Resolver | MySQL (`bank_gateway_db`) + Redis | **BROKEN BUILD** (Lombok setter mismatch) |
| **Settlement** | `8092` | Daily Merchant Settlement & GST Calc | MySQL (`settlement_db`) | **TEST FAILED** (NullPointerException) |
| **Reconciliation**| `8093` | EOD Statement vs Ledger Audit | MySQL (`reconciliation_db`) | **BROKEN BUILD** (Undeclared methods/exceptions) |
| **Kyc** | `8094` | UIDAI/NSDL KYC & AES Encryption | MySQL (`kyc_db`) + Redis | Functional (Orphaned from Gateway) |
| **Aml** | `8095` | Velocity Checks & Watchlist Screening | MySQL (`aml_db`) + Redis | Functional (Orphaned from Gateway) |
| **Risk** | `8096` | Multi-Factor Composite Risk Scoring | MySQL (`risk_db`) + Redis | **TEST FAILED** (Floating point assert error) |
| **Subscription** | `8098` | UPI AutoPay & Dunning Retries | MySQL (`subscription_db`) | Functional |
| **Invoice** | `8099` | B2B Invoicing & PDF Generation | MySQL (`invoice_db`) | Functional (Orphaned from Gateway) |
| **Dispute** | `8100` | Dispute Arbitration & Chargebacks | MySQL (`dispute_db`) | Functional |
| **Rewards** | `8101` | Loyalty Points & Cashback Ledger | MySQL (`rewards_db`) + Redis | **NON-RUNNABLE STUB** (No `@SpringBootApplication`) |
| **Loan** | `8102` | BNPL Lending & EMI Calculations | MySQL (`loan_db`) | Functional |
| **Referral** | `8103` | Referral Codes & Fraud Defense | MySQL (`referral_db`) + Redis | Functional (Calls non-functional Rewards) |
| **Payroll** | `8104` | Batch Salary Calculation & Slips | MySQL (`payroll_db`) | Functional |
| **Analytics** | `8105` | Transaction & User Metrics Aggregation | MySQL (`analytics_db`) + Redis | **INCOMPLETE STUB** (0 controllers, 0 services) |

---

## 4. Data Flow (CURRENT)

### 4.1 Online Payment Transaction Flow
1. **Client Request:** Client sends `POST /payment/pay` with Bearer token to API Gateway (Port `8080`).
2. **Gateway Processing:** Gateway's `JwtAuthenticationFilter` validates the JWT token, extracts `userId`, and forwards request to Payment Service (Port `8083`) with header `X-User-Id`.
3. **Idempotency Check:** `PaymentService` verifies idempotency key against MySQL (`payments` table).
4. **Pre-Debit Balance Check:** `PaymentService` invokes `WalletServiceClient.getBalance(senderId)` via Feign over Eureka.
5. **Signature Creation:** Signs payload with RSA private key.
6. **Persistence:** Saves record to MySQL with status `INITIATED` and **hardcodes** `.fraudStatus(Payment.FraudStatus.SAFE)` (line 89).
7. **Event Publishing:** Emits `PaymentEvent` to Kafka topic `payment_initiated`.
8. **Wallet Execution:** `WalletKafkaConsumer` consumes `payment_initiated`:
   - Debits sender wallet in MySQL (`wallets`).
   - Credits receiver wallet in MySQL.
   - Publishes `PaymentEvent` to Kafka topic `payment_completed` (or `payment_failed` on error).
9. **Ledger Recording:** `TransactionKafkaConsumer` consumes `payment_completed` and writes immutable transaction records to MySQL and MongoDB.
10. **Notification:** `NotificationKafkaConsumer` consumes `payment_completed`, calls `AuthServiceClient` to fetch email/phone, and dispatches HTML email and SMS alert.

### 4.2 Offline Payment Sync Flow
1. Client signs payment locally while offline.
2. When network reconnects, client calls `POST /sync/process` on Sync Service (Port `8089`).
3. Sync Service calls `PaymentServiceClient.processOfflinePayment(...)` via Feign.
4. Sync Service publishes to Kafka topic `sync_completed`.
5. Transaction Service and Notification Service consume `sync_completed` to record the ledger entry and alert the user.

### 4.3 Recurring AutoPay Mandate Flow
1. Client calls `POST /subscription/create` on Subscription Service (Port `8098`).
2. Subscription Service calls `NpciServiceClient.createMandate(...)` via Feign.
3. Quartz scheduler triggers `execute-scheduled` charge; calls NPCI `POST /npci/initiate-transaction`.
4. If payment fails, 3-step dunning retry schedule is executed before auto-cancelling the mandate.

---

## 5. Technology Map

- **Runtime:** Java 17 (Eclipse Adoptium JDK 17.0.18+8)
- **Framework:** Spring Boot 3.2.5, 3.3.2, 3.3.5, 3.5.0, 4.0.6 (Eureka)
- **Service Discovery:** Spring Cloud Netflix Eureka Server / Client (Spring Cloud 2023.x)
- **API Gateway:** Spring Cloud Gateway (Project Reactor / WebFlux)
- **Databases:**
  - **Relational:** MySQL 8.x (21 dedicated database schemas)
  - **NoSQL Document:** MongoDB 7.x (`upi_ai_db`, `upi_fraud_db`, `upi_transaction_audit_db`)
- **Caching & Rate Limiting:** Redis 7.x (Lettuce / Spring Data Redis Reactive)
- **Message Broker:** Apache Kafka (Confluent Platform 7.5.0) + Zookeeper 7.5.0
- **Resilience:** Resilience4j (Circuit Breaker, Retry, Rate Limiter)
- **Security:** Spring Security 6.x, JJWT (0.12.5), BCrypt, AES-256-GCM
- **Document & Media Generation:** iText7 (PDF Invoices and Salary Slips), Apache POI 5.2.5 (Excel Reconciliation Reports), ZXing 3.5.3 (QR Code Generation)
- **AI Integration:** Spring AI 1.0.0-M1 (OpenAI ChatClient)

---

## 6. Fraud / Risk Audit Findings

Inspection of `Fraud`, `Risk`, `Aml`, `Analytics`, `Transaction`, `Payment`, and `AiService` revealed the following:

| Service | Intended Role | Actual Verification Finding |
|---|---|---|
| **Fraud** | Real-time payment screening rules | Operates on **in-memory data structures** (`CopyOnWriteArrayList`), not persistent database. Fully disconnected from `PaymentService` (bypassed in code). NPCI calls non-existent path `/fraud/internal/check` (404 caught by fallback). |
| **Risk** | ML-like heuristic behavioral risk scoring | Comprehensive evaluation engine (device fingerprint SHA-256, location anomaly, unusual hours, 3x amount spikes). Saves to MySQL (`risk_db`). **Completely orphaned**: not called by Payment, NPCI, or Gateway. Unit test fails due to floating point comparison without delta. |
| **Aml** | PMLA 2002 compliance & watchlist screening | Production-grade Levenshtein distance matching against PEP/UN/OFAC watchlists, plus velocity & structuring checks. Saves to MySQL (`aml_db`). **Completely orphaned**: no service invokes it and Gateway does not route to it. |
| **Analytics** | Metric computation & trend detection | **Non-operational stub**: defines entity and Feign clients, but has 0 controllers, 0 services, and 0 cron jobs. |
| **Transaction**| Ledger history & spend analysis | Records completed transactions. Calculates spend summaries. Compilation is currently broken due to a syntax error. |
| **Payment** | Fraud enforcement | `PaymentService.java:89` explicitly hardcodes `.fraudStatus(Payment.FraudStatus.SAFE)` and skips fraud checks before emitting to Kafka. |
| **AiService** | Explainable AI for blocked payments | Endpoint `/ai/fraud-explain` exists. Passes parameters to OpenAI GPT model with fallback to canned text. |

### Redundant Capabilities Identified
- **Velocity Tracking:** Triplicated across `Fraud` (`RapidTransactionRule`), `Aml` (`VelocityCheckService`), and `NPCI` (`NpciTransactionService`).
- **High-Value Threshold (> ₹50,000):** Duplicated in `Fraud` (`HighAmountRule`), `Risk` (`RiskScoringService`), and `Aml` (`AmlService`).
- **Unusual Hours Screening:** Duplicated in `Fraud` (`UnusualTimeRule`) and `Risk` (`BehavioralAnalysisService`).

---

## 7. AI Audit Findings (`AiService`)

- **Configured Models / Providers:** Spring AI configured with OpenAI `gpt-3.5-turbo`. The API key is specified as `${OPENAI_API_KEY:mock-key}`.
- **Is AI Actually Functional?** **No, in ordinary operation.** Unless a valid external `OPENAI_API_KEY` environment variable is provided, all OpenAI API calls throw an authentication error. The application catches this exception and returns hardcoded fallback strings:
  - Chat fallback: `"Sorry, I am unable to process your request right now. Please try again later."`
  - Fraud explain fallback: `"Your payment was flagged for security reasons. Please contact support."`
- **Hardcoded Financial Data:** In `AiService.java:130-138`, the method `analyzeExpenses()` sends a completely hardcoded string to the model:
  ```
  Last 30 days transactions:
  - Food: ₹4500
  - Shopping: ₹8200
  - Travel: ₹2100
  - Entertainment: ₹1800
  - Utilities: ₹3200
  Total Spend: ₹19800
  ```
  It does not query `TransactionService` or any database for the user's true transactions.
- **Tools & RAG:** Zero AI tools, zero function calling callbacks, zero vector embeddings, and zero retrieval-augmented generation (RAG) exist.

---

## 8. Database Audit & Data Ownership

### 8.1 Schema Isolation
- Each service connects to its own isolated database name.
- **No shared database anti-pattern was detected.**
- All 21 MySQL services use `spring.jpa.hibernate.ddl-auto: update`.
- **Zero migration tools:** Neither Flyway nor Liquibase is configured anywhere in the project. Schema evolution is unversioned.

### 8.2 Entity Ownership Map

| Database Name | Owning Service | Entities / Tables |
|---|---|---|
| `upi_auth_db` | Auth | `users`, `roles`, `refresh_tokens` |
| `upi_wallet_db` | Wallet | `wallets`, `wallet_transactions` |
| `upi_payment_db` | Payment | `payments` |
| `upi_transaction_db` | Transaction | `transactions` (MySQL) + `audit_logs`, `payment_logs` (MongoDB) |
| `upi_merchant_db` | Merchant | `merchants`, `merchant_qr_codes` |
| `upi_sync_db` | Sync | `sync_records` |
| `npci_db` | NPCI | `refund_records`, `upi_mandated`, `upi_transactions` |
| `bank_gateway_db` | BankGateway | `ifsc_details`, `linked_bank_accounts`, `upi_handle_resolutions` |
| `settlement_db` | Settlement | `merchant_settlements`, `settlement_batches`, `settlement_transactions` |
| `reconciliation_db`| Reconciliation | `reconciliation_reports`, `reconciliation_discrepancies` |
| `kyc_db` | Kyc | `kyc_records`, `kyc_documents`, `kyc_audit_logs` |
| `aml_db` | Aml | `aml_alerts`, `aml_screening_results`, `watchlist_entries` |
| `risk_db` | Risk | `risk_profiles`, `risk_scoring_results` |
| `subscription_db` | Subscription | `subscriptions`, `subscription_payment_attempts` |
| `invoice_db` | Invoice | `invoices`, `invoice_line_items` |
| `dispute_db` | Dispute | `disputes`, `dispute_evidences` |
| `rewards_db` | Rewards | `offers`, `reward_ledger`, `user_reward_accounts` |
| `loan_db` | Loan | `loan_applications`, `loan_repayments` |
| `referral_db` | Referral | `referral_codes`, `referral_records` |
| `payroll_db` | Payroll | `payroll_batches`, `employee_payments` |
| `analytics_db` | Analytics | `daily_metrics`, `hourly_metrics` |
| `upi_ai_db` | AiService | `chat_history` (MongoDB) |
| `upi_fraud_db` | Fraud | `fraud_logs` (MongoDB / In-Memory) |

---

## 9. Docker & Infrastructure Audit

1. **Dockerfiles:** **ZERO Dockerfiles exist** in the repository. Neither root nor any microservice has a `Dockerfile`.
2. **Docker Compose:** Only 2 local `docker-compose.yml` files exist:
   - `Payment/Payment/docker-compose.yml`
   - `Wallet/Wallet/docker-compose.yml`
   Both contain identical definitions for Confluent Zookeeper (`2181:2181`) and Kafka (`9092:9092`).
3. **Containerized Infrastructure:** Only Kafka and Zookeeper are declared in compose files. MySQL, Redis, MongoDB, Eureka, Gateway, and the microservices are not containerized.
4. **Root Compose:** No root-level `docker-compose.yml` orchestrating the platform exists.

---

## 10. Security Audit Findings

1. **Hardcoded Secrets & Plaintext Credentials:**
   - Eureka Server credentials: `admin:admin123` hardcoded in `Eureka/src/main/resources/application.yml` and in client configs across all 25 microservices (`SECRET DETECTED — VALUE REDACTED`).
   - Default JWT Signing Secret: Hardcoded fallback key `YourSuperSecretKeyHereMustBe256BitsLongForHS256AlgorithmSecurity` present in `Gateway` and `Auth` (`SECRET DETECTED — VALUE REDACTED`).
   - Database credentials: Default passwords `root` or `admin` present in configuration files (`SECRET DETECTED — VALUE REDACTED`).
   - NPCI Keystore/Truststore: Hardcoded passwords in `NPCI/src/main/resources/application.yaml` (`SECRET DETECTED — VALUE REDACTED`).
2. **Perimeter vs Downstream Authorization:**
   - Downstream services (`Wallet`, `Sync`, `Transaction`, `Fraud`, `Merchant`, `Notification`, `NPCI`, `Reconciliation`, `AiService`) configure `.anyRequest().permitAll()`.
   - If an attacker gains internal network access or bypasses the gateway, they can execute unrestricted balance debits, credits, or data inspection without authentication.
3. **CORS Configuration:**
   - `Gateway` defines `globalcors.cors-configurations.'[/**]'.allowedOrigins: "*"` with wildcard origins.

---

## 11. Build Verification & Testing Status

### 11.1 Compilation Verification (`mvn test-compile -DskipTests`)
- **Compiled Successfully (19 Services):** `Aml`, `Analytics`, `Dispute`, `Eureka`, `Fraud`, `Invoice`, `Kyc`, `Loan`, `Merchant`, `Notification`, `Payment`, `Payroll`, `Referral`, `Rewards`, `Risk`, `Settlement`, `Subscription`, `Sync`, `Wallet`.
- **Failed Compilation (7 Services):**
  1. `AiService` — `testCompile` failed: `package org.junit.jupiter.api does not exist` (missing `spring-boot-starter-test` in POM).
  2. `Auth` — `testCompile` failed: `cannot find symbol: class SpringBootTest` (missing `spring-boot-starter-test` in POM).
  3. `BankGateway` — `compile` failed: `cannot find symbol: method setIsActive(boolean)` in `UpiResolveService.java` (Lombok field `isActive` generates `setActive`).
  4. `Gateway` — `testCompile` failed: `package org.junit.jupiter.api does not exist` (missing `spring-boot-starter-test` in POM).
  5. `NPCI` — `compile` failed: `cannot find symbol: class TransactionStatus` in `NpciTransactionService.java:240` and missing exception classes in `GlobalExceptionHandler.java`.
  6. `Reconciliation` — `compile` failed: undefined `SettlementProcessingException` and undeclared method `findByDiscrepancyId` in `ReconciliationDiscrepancyRepository`.
  7. `Transaction` — `compile` failed: syntax error in `TransactionRepository.java:49` (missing `@` character before `Param("from")`).

### 11.2 Automated Test Execution (`mvn test`)
- **Passed Automated Tests (15 Services):** `Aml` (6/6 passed), `Dispute` (6/6 passed), `Eureka` (contextLoads), `Fraud` (contextLoads), `Invoice` (5/5 passed), `Kyc` (6/6 passed), `Loan` (11/11 passed), `Merchant` (contextLoads), `Notification` (contextLoads), `Payment` (contextLoads), `Payroll` (4/4 passed), `Referral` (12/12 passed), `Subscription` (6/6 passed), `Sync` (contextLoads), `Wallet` (contextLoads).
- **Failed Automated Tests (2 Services):**
  1. `Risk` — `RiskScoringServiceTest.testNewDeviceBoostTriggered` failed: `expected: <0.3> but was: <0.30000000000000004>` (floating point arithmetic precision error).
  2. `Settlement` — `SettlementServiceTest.handlesPartialMerchantFailure` failed: `NullPointerException: Cannot read field "intCompact" because "subtrahend" is null` in `BigDecimal.subtract`.
- **Zero Tests / Non-Runnable (2 Services):**
  1. `Analytics` — 0 test files.
  2. `Rewards` — 0 test files; no `@SpringBootApplication` entrypoint.

---

## 12. Technical Debt Matrix

| Category | Severity | Description | Impact |
|---|---|---|---|
| **Broken Compilation** | CRITICAL | 7 services fail compilation or test compilation (`BankGateway`, `NPCI`, `Reconciliation`, `Transaction`, `Auth`, `Gateway`, `AiService`). | Platform cannot build cleanly or run CI/CD. |
| **Contract Mismatches** | CRITICAL | Feign clients in `NPCI`, `Settlement`, `Reconciliation`, `Payment`, `Dispute`, and `Referral` target missing REST endpoints. | Inter-service calls return 404 errors at runtime. |
| **Pipeline Disconnection**| HIGH | `PaymentService` hardcodes fraud status `SAFE` and does not invoke `Fraud` or `Risk` before emitting `payment_initiated`. | Fraud and risk scoring subsystems are bypassed. |
| **Orphaned Services** | HIGH | `Aml`, `Risk`, `Kyc`, and `Invoice` have no routes in Gateway and no inbound service calls. | Valuable business logic is inaccessible. |
| **Security Exposure** | HIGH | Permissive `.anyRequest().permitAll()` in downstream services; hardcoded Eureka credentials across all 26 services. | Internal services lack defense-in-depth authorization. |
| **Missing Migrations** | MEDIUM | No Flyway/Liquibase; all 21 MySQL services use `hibernate.ddl-auto: update`. | Uncontrolled schema changes; high production data loss risk. |
| **Missing Unit Tests** | MEDIUM | Core services (`Auth`, `Payment`, `Wallet`, `Transaction`, `Notification`, `Merchant`) only possess empty `contextLoads()` tests. | Inability to refactor safely without regression risk. |
| **Stub Services** | MEDIUM | `Analytics` has no controllers or business logic; `Rewards` has no main class. | Incomplete system capabilities. |

---

## 13. Recommended Evolution (PLANNED — Phase 02 Scope)

The following architectural evolution plan is strictly **PLANNED** for Phase 02 and represents forward-looking engineering recommendations:

```
[ PHASE 01: AUDIT & RECOVERY ]  ───>  [ PHASE 02: RESTORATION & UNIFICATION ]
      (Current State)                               (Planned Target)
- 7 Broken Builds                             - 100% Green Compilation
- Contract Mismatches                         - Aligned Feign & REST Contracts
- Disconnected Fraud Pipeline                 - Synchronous SentinelX Pre-Payment Hook
- 14 Unrouted Services                        - Complete Gateway Routing & Auth Filter
- No DB Migration Tool                        - Flyway Versioned Migrations
- Mocked AI Financial Data                    - Agentic AI with Real Transaction Tools
```

### Planned Work Packages for Phase 02:
1. **Compilation & Build Repair:**
   - Add `spring-boot-starter-test` to `AiService`, `Auth`, and `Gateway` POMs.
   - Fix Lombok boolean getter/setter in `BankGateway` (`setActive`).
   - Fix missing `TransactionStatus` import and exception classes in `NPCI`.
   - Fix syntax error in `TransactionRepository` (add missing `@`).
   - Fix undeclared repository method and exception import in `Reconciliation`.
2. **Contract Alignment & Gateway Routing:**
   - Align all Feign client interface signatures with actual controller request mappings across `NPCI`, `Settlement`, `Reconciliation`, `Wallet`, and `Notification`.
   - Add missing route mappings in `Gateway` for all 14 previously unrouted services.
   - Introduce `@SpringBootApplication` and REST controllers to `Rewards` and implement the `Analytics` pipeline.
3. **Risk & Fraud Integration (SentinelX Engine):**
   - Wire `Risk` and `Fraud` into the pre-authorization hook in `PaymentService` before `payment_initiated` is emitted.
   - Unify velocity, watchlist, and behavioral scoring into a cohesive real-time evaluation flow.
4. **Database & Security Hardening:**
   - Replace `ddl-auto: update` with Flyway database migration scripts.
   - Externalize all hardcoded credentials into environment variables and secrets managers.
   - Enforce internal service authentication (`X-Internal-Service-Key` or mutual TLS/JWT).
