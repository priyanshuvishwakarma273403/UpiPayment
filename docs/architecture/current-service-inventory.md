# UPI MESH — CURRENT SERVICE INVENTORY

> **Audit Timestamp:** September 2026  
> **Target Repository:** `d:\UpiMesh`  
> **Phase:** 01 — Existing System Audit & Architecture Recovery  
> **Inspection Method:** Static code analysis of POMs, resources, Java sources, configurations, and build/test execution.  
> **Integrity Standard:** Only code-verified facts are stated. Missing/unverified items are explicitly marked as `UNVERIFIED`.

---

## 1. Inventory Summary Matrix

| # | Service Name | Directory | Port | DB Type / Name | Eureka App Name | Build Status |
|---|---|---|---|---|---|---|
| 1 | **AiService** | `AiService/AiService` | `8087` | MongoDB (`upi_ai_db`) | `ai-service` | ❌ FAILED (`testCompile`) |
| 2 | **Aml** | `Aml/Aml` | `8095` | MySQL (`aml_db`) | `aml-service` | ✅ SUCCESS (Tests Pass) |
| 3 | **Analytics** | `Analytics/Analytics` | `8105` | MySQL (`analytics_db`) | `analytics-service` | ⚠️ STUB (0 Endpoints, 0 Logic) |
| 4 | **Auth** | `Auth/Auth` | `8081` | MySQL (`upi_auth_db`) | `auth-service` | ❌ FAILED (`testCompile`) |
| 5 | **BankGateway** | `BankGateway/BankGateway` | `8091` | MySQL (`bank_gateway_db`) | `bank-gateway-service` | ❌ FAILED (Compile) |
| 6 | **Dispute** | `Dispute/Dispute` | `8100` | MySQL (`dispute_db`) | `dispute-service` | ✅ SUCCESS (Tests Pass) |
| 7 | **Eureka** | `Eureka/Eureka` | `8761` | None | `eureka-server` | ✅ SUCCESS |
| 8 | **Fraud** | `Fraud/Fraud` | `8086` | In-Memory / MongoDB (`upi_fraud_db`) | `fraud-service` | ✅ SUCCESS |
| 9 | **Gateway** | `Gateway/Gateway` | `8080` | None | `api-gateway` | ❌ FAILED (`testCompile`) |
| 10 | **Invoice** | `Invoice/Invoice` | `8099` | MySQL (`invoice_db`) | `invoice-service` | ✅ SUCCESS (Tests Pass) |
| 11 | **Kyc** | `Kyc/Kyc` | `8094` | MySQL (`kyc_db`) | `kyc-service` | ✅ SUCCESS (Tests Pass) |
| 12 | **Loan** | `Loan/Loan` | `8102` | MySQL (`loan_db`) | `loan-service` | ✅ SUCCESS (Tests Pass) |
| 13 | **Merchant** | `Merchant/Merchant` | `8085` | MySQL (`upi_merchant_db`) | `merchant-service` | ✅ SUCCESS |
| 14 | **NPCI** | `NPCI/NPCI` | `8090` | MySQL (`npci_db`) | `npci-integration-service` | ❌ FAILED (Compile) |
| 15 | **Notification** | `Notification/Notification` | `8088` | None | `notification-service` | ✅ SUCCESS |
| 16 | **Payment** | `Payment/Payment` | `8083` | MySQL (`upi_payment_db`) | `payment-service` | ✅ SUCCESS |
| 17 | **Payroll** | `Payroll/Payroll` | `8104` | MySQL (`payroll_db`) | `payroll-service` | ✅ SUCCESS (Tests Pass) |
| 18 | **Reconciliation** | `Reconciliation/Reconciliation` | `8093` | MySQL (`reconciliation_db`) | `reconciliation-service` | ❌ FAILED (Compile) |
| 19 | **Referral** | `Referral/Referral` | `8103` | MySQL (`referral_db`) | `referral-service` | ✅ SUCCESS (Tests Pass) |
| 20 | **Rewards** | `Rewards/Rewards` | `8101` | MySQL (`rewards_db`) | `rewards-service` | ⚠️ STUB (No Main Class) |
| 21 | **Risk** | `Risk/Risk` | `8096` | MySQL (`risk_db`) | `risk-scoring-service` | ❌ TEST FAILED |
| 22 | **Settlement** | `Settlement/Settlement` | `8092` | MySQL (`settlement_db`) | `settlement-service` | ❌ TEST FAILED |
| 23 | **Subscription** | `Subscription/Subscription` | `8098` | MySQL (`subscription_db`) | `subscription-service` | ✅ SUCCESS (Tests Pass) |
| 24 | **Sync** | `Sync/Sync` | `8089` | MySQL (`upi_sync_db`) | `sync-service` | ✅ SUCCESS |
| 25 | **Transaction** | `Transaction/Transaction` | `8084` | MySQL (`upi_transaction_db`) + MongoDB | `transaction-service` | ❌ FAILED (Compile) |
| 26 | **Wallet** | `Wallet/Wallet` | `8082` | MySQL (`upi_wallet_db`) | `wallet-service` | ✅ SUCCESS |

---

## 2. Comprehensive Service Catalog

### 1. AiService
- **Service:** AiService
- **Purpose:** Conversational payment assistant, spending pattern analysis, and natural-language explanations of fraud block decisions.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring AI (OpenAI ChatClient), Spring Data MongoDB, Eureka Client.
- **Port:** `8087`
- **Database:** MongoDB (`upi_ai_db`), collection: `chat_history`.
- **Dependencies:** `org.springframework.boot:spring-boot-starter-web`, `org.springframework.boot:spring-boot-starter-data-mongodb`, `org.springframework.ai:spring-ai-openai-spring-boot-starter`, `org.springframework.cloud:spring-cloud-starter-netflix-eureka-client`, `org.projectlombok:lombok`. *(Note: `spring-boot-starter-test` is omitted from `pom.xml`)*.
- **Inbound APIs:**
  - `POST /ai/chat` — Accepts `ChatRequest`, requires header `X-User-Id`, maintains conversation history in MongoDB.
  - `POST /ai/expense-analysis` — Accepts `ExpenseAnalysisRequest`, requires header `X-User-Id`.
  - `POST /ai/fraud-explain` — Accepts payload with `paymentId`, `fraudDecision`, `reasons`, and `riskScore`.
- **Outbound APIs:** External OpenAI API (`https://api.openai.com/v1/chat/completions`). UNVERIFIED (No inter-service Feign/REST clients).
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** UNVERIFIED (None)
- **Authentication:** `SecurityConfig` defines `anyRequest().permitAll()`. Unauthenticated at the transport layer; expects `X-User-Id` header from API Gateway.
- **Current status:** Partially functional / Mocked. Compiles `src/main`, but `mvn test-compile` fails due to missing test runner dependencies. `analyzeExpenses()` contains completely hardcoded spending text (`Food: ₹4500... Total: ₹19800`) and does not query actual transactions. Calls to OpenAI fail unless an external `OPENAI_API_KEY` is provided, triggering a canned fallback response.
- **Problems:** Hardcoded dummy financial data; missing test framework in pom; unauthenticated API access; zero AI function-calling/tools.
- **Recommended future role:** Evolve into an intelligent agentic copilot linked directly to the Transaction and Analytics services.

---

### 2. Aml
- **Service:** Aml
- **Purpose:** Anti-Money Laundering screening complying with PMLA 2002 guidelines: velocity monitoring, structuring (smurfing) pattern detection, and fuzzy watchlist screening (Levenshtein distance $\ge$ 80%) against UN/OFAC/PEP databases.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, Redis (Lettuce), Eureka Client, Apache Commons Text.
- **Port:** `8095`
- **Database:** MySQL (`aml_db`), tables: `aml_alerts`, `aml_screening_results`, `watchlist_entries`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `mysql-connector-j`, `spring-cloud-starter-netflix-eureka-client`, `commons-text`, `lombok`.
- **Inbound APIs:**
  - `POST /aml/screen` — Screens a transaction against velocity, structuring, and watchlist rules.
  - `GET /aml/alerts/{userUpiId}` — Retrieves AML alerts for a user.
  - `PUT /aml/alert/{alertId}/resolve` — Resolves an alert.
  - `PUT /aml/alert/{alertId}/escalate` — Escalates an alert for compliance officer review.
  - `POST /aml/watchlist` — Ingests a new watchlist entry.
  - `GET /aml/screening/{transactionId}` — Retrieves an audit screening record.
- **Outbound APIs:** UNVERIFIED (None)
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** `VelocityCheckService` maintains sliding hourly/daily transaction counters with TTLs.
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT verification.
- **Current status:** Compiles cleanly and all 6 unit tests pass. However, it is an **ORPHANED SERVICE**: not configured in API Gateway routes, and no microservice calls it.
- **Problems:** Completely isolated from live payment flow; duplicates velocity rules present in Fraud and NPCI services.
- **Recommended future role:** Primary asynchronous compliance screening engine triggered via Kafka events from the payment pipeline.

---

### 3. Analytics
- **Service:** Analytics
- **Purpose:** Intended to aggregate hourly and daily transaction metrics, active user trends, and platform revenue metrics.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, Spring Data Redis, OpenFeign, Eureka Client.
- **Port:** `8105`
- **Database:** MySQL (`analytics_db`), tables: `daily_metrics`, `hourly_metrics`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:** UNVERIFIED (None — No controllers exist).
- **Outbound APIs:**
  - `AuthServiceClient` (Feign -> `auth-service`): `GET /auth/users/count/new`, `GET /auth/users/count/active`
  - `TransactionServiceClient` (Feign -> `transaction-service`): `GET /transactions/debit-between`
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Configured in `RedisConfig`, but has no active business usages.
- **Authentication:** UNVERIFIED (No custom security filter chain).
- **Current status:** **INCOMPLETE STUB**. Compiles, but contains 0 controllers, 0 service classes, and 0 tests.
- **Problems:** Completely non-operational skeleton; Gateway route `/analytics/**` leads to 404 for all requests.
- **Recommended future role:** Build scheduled batch aggregators and Kafka consumers to compute real-time platform metrics.

---

### 4. Auth
- **Service:** Auth
- **Purpose:** Core identity provider: user signup/login, OTP generation/validation via Redis, rate limiting, and JWT issuance with refresh token rotation.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Security, Spring Data JPA, MySQL, Redis, Eureka Client, jjwt.
- **Port:** `8081`
- **Database:** MySQL (`upi_auth_db`), tables: `users`, `roles`, `refresh_tokens`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `mysql-connector-j`, `spring-cloud-starter-netflix-eureka-client`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson`, `lombok`. *(Note: `spring-boot-starter-test` missing from `pom.xml`)*.
- **Inbound APIs:**
  - `POST /auth/register` — User signup (assigns `<phone>@upimesh` handle)
  - `POST /auth/login` — Authentication via password
  - `POST /auth/verify-otp` — Validates 6-digit Redis OTP
  - `POST /auth/resend-otp` — Resends OTP (rate limited to 3 per 15 min)
  - `POST /auth/refresh-token` — Rotates refresh token & issues new JWT
  - `GET /auth/users/{id}` — Internal user lookup by ID
  - `GET /auth/users/upi/{upiId}` — Internal user lookup by UPI ID
  - `GET /auth/users/count/new` — User registration metrics
  - `GET /auth/users/count/active` — Active user metrics
- **Outbound APIs:** UNVERIFIED (None)
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** `OtpService` stores OTP codes with 5-minute TTL and tracks attempt counts.
- **Authentication:** Spring Security with `BCryptPasswordEncoder`. Public endpoints for register/login/OTP; internal endpoints unprotected if accessed directly.
- **Current status:** Compiles `src/main`, but `testCompile` fails due to missing `spring-boot-starter-test`.
- **Problems:** Missing test dependency; nested `.git` directory; hardcoded default JWT secret in configuration (`SECRET DETECTED — VALUE REDACTED`).
- **Recommended future role:** Central identity authority and token signing service for the ecosystem.

---

### 5. BankGateway
- **Service:** BankGateway
- **Purpose:** Core banking simulation implementing strategy pattern for Indian banks (HDFC, SBI, ICICI, etc.), bank account linking, balance inquiry, IFSC resolution, and UPI handle resolution.
- **Technology:** Spring Boot 3.3.2, Java 17, Spring Data JPA, MySQL, Redis, OpenFeign, Eureka Client.
- **Port:** `8091`
- **Database:** MySQL (`bank_gateway_db`), tables: `ifsc_details`, `linked_bank_accounts`, `upi_handle_resolutions`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-openfeign`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:**
  - `POST /bank/accounts/link` — Link user bank account
  - `GET /bank/accounts/{userUpiId}` — Fetch linked bank accounts
  - `POST /bank/accounts/balance` — Verify account balance
  - `PUT /bank/accounts/{accountId}/set-primary` — Set primary payment account
  - `DELETE /bank/accounts/{accountId}` — Unlink account
  - `GET /bank/ifsc/{ifscCode}` — Resolve IFSC details
  - `GET /bank/upi/resolve/{upiHandle}` — Resolve VPA to account details
- **Outbound APIs:** UNVERIFIED (Uses internal mock bank clients via `BankClientRegistry`).
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Balance query caching and IFSC code cache.
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT verification.
- **Current status:** **COMPILATION BROKEN** (`EXIT 1`). `UpiResolveService.java` calls `setIsActive(boolean)` on `UpiHandleResolution`, but Lombok generates `setActive(boolean)` for boolean field `isActive`.
- **Problems:** Fails compilation; Gateway lacks route to port 8091; Settlement service Feign client calls non-existent endpoint `/bank/accounts/primary/{merchantUpiId}`.
- **Recommended future role:** Fix setter invocation; serve as the core bank integration adapter.

---

### 6. Dispute
- **Service:** Dispute
- **Purpose:** Manages payment disputes, evidence upload, merchant rebuttal submissions, and automated arbitration/refunds via NPCI.
- **Technology:** Spring Boot 3.3.2, Java 17, Spring Data JPA, MySQL, Redis, OpenFeign, Eureka Client.
- **Port:** `8100`
- **Database:** MySQL (`dispute_db`), tables: `disputes`, `dispute_evidences`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:**
  - `POST /dispute/raise` — File new transaction dispute
  - `POST /dispute/{disputeId}/merchant-response` — Submit merchant rebuttal
  - `POST /dispute/{disputeId}/resolve` — Arbitrate dispute (approves refund or dismisses)
  - `GET /dispute/{disputeId}` — Dispute details
  - `GET /dispute/user/{userUpiId}` — User disputes
  - `GET /dispute/merchant/{merchantUpiId}` — Merchant disputes
- **Outbound APIs:**
  - `NotificationServiceClient` (Feign -> `notification-service`): `POST /notification/internal/payment` *(Endpoint missing in target!)*
  - `NpciServiceClient` (Feign -> `npci-integration-service`): `GET /npci/check-status/{transactionId}`, `POST /npci/refund`
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Configured in `AppConfig`.
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT enforcement.
- **Current status:** Compiles cleanly and all 6 unit tests pass.
- **Problems:** Gateway has no route to `/dispute/**`; `NotificationServiceClient` targets an endpoint that does not exist in `NotificationController`.
- **Recommended future role:** Customer dispute arbitration and chargeback engine.

---

### 7. Eureka
- **Service:** Eureka
- **Purpose:** Netflix Eureka Service Registry providing dynamic service discovery, registry synchronization, and health monitoring.
- **Technology:** Spring Boot 4.0.6, Spring Cloud Netflix Eureka Server, Java 17.
- **Port:** `8761`
- **Database:** None
- **Dependencies:** `spring-cloud-starter-netflix-eureka-server`, `spring-boot-starter-security`.
- **Inbound APIs:** Eureka dashboard and discovery registration endpoints (`/eureka/**`).
- **Outbound APIs:** None
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** None
- **Authentication:** Basic Auth configured: username `admin`, password `SECRET DETECTED — VALUE REDACTED`.
- **Current status:** Compiles cleanly and contextLoads test passes.
- **Problems:** Hardcoded basic auth credentials; nested `.git` repository folder.
- **Recommended future role:** Continue as service discovery backbone.

---

### 8. Fraud
- **Service:** Fraud
- **Purpose:** Real-time rule-based fraud screening: Rapid Transactions (velocity), Repeated Transactions (duplicate amounts), High Amount threshold (> ₹50,000), and Unusual Hours (11 PM - 5 AM).
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data Redis, Spring Kafka, Eureka Client.
- **Port:** `8086`
- **Database:** In-memory repository (`CopyOnWriteArrayList` in `FraudLogRepository`); MongoDB configured in properties (`upi_fraud_db`) but not bound via repository.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-redis`, `spring-kafka`, `spring-cloud-starter-netflix-eureka-client`, `lombok`.
- **Inbound APIs:**
  - `POST /fraud/check` — Synchronous fraud evaluation
  - `GET /fraud/logs/{paymentId}` — View fraud check log
  - `GET /fraud/history/{senderId}` — Check sender fraud history
  - `GET /fraud/high-risk` — List high-risk flagged transactions
  - `GET /fraud/stats` — Fraud engine summary stats
- **Outbound APIs:** None
- **Kafka producer:** `KafkaConfig` defines `KafkaTemplate`, but no classes publish events.
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Used by `RapidTransactionRule` and `RepeatedTransactionRule` to track transaction frequency.
- **Authentication:** `SecurityConfig` specifies `.anyRequest().permitAll()`.
- **Current status:** Compiles cleanly and contextLoads test passes.
- **Problems:** Disconnected from live payment processing (`PaymentService` bypasses it); NPCI Feign client calls non-existent path `/fraud/internal/check`; logs stored in non-persistent memory list.
- **Recommended future role:** Merge with Risk and Aml to form a unified SentinelX risk detection engine.

---

### 9. Gateway
- **Service:** Gateway
- **Purpose:** API Gateway reverse proxy, global CORS management, JWT authentication and header propagation, Redis-backed rate limiting, and Resilience4j circuit breaking.
- **Technology:** Spring Boot 3.3.5, Spring Cloud Gateway (Reactive), Reactive Redis, Resilience4j, Eureka Client, jjwt.
- **Port:** `8080`
- **Database:** None
- **Dependencies:** `spring-cloud-starter-gateway`, `spring-boot-starter-data-redis-reactive`, `spring-cloud-starter-circuitbreaker-reactor-resilience4j`, `spring-cloud-starter-netflix-eureka-client`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson`, `lombok`. *(Note: `spring-boot-starter-test` missing from `pom.xml`)*.
- **Inbound APIs:** Routes 10 services (`/auth/**`, `/wallet/**`, `/payment/**`, `/transactions/**`, `/payroll/**`, `/analytics/**`, `/merchant/**`, `/fraud/**`, `/ai/**`, `/sync/**`).
- **Outbound APIs:** Load balanced calls to registered services via `lb://`.
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Reactive Redis used for token bucket rate limiting.
- **Authentication:** `JwtAuthenticationFilter` intercepts requests, extracts Bearer token, validates signature, and injects `X-User-Id`, `X-User-Email`, `X-User-Roles` headers.
- **Current status:** Compiles `src/main`, but `testCompile` fails due to missing test dependency.
- **Problems:** 14 backend microservices lack gateway routes; missing test dependency; wildcard CORS configuration (`allowedOrigins: "*"`).
- **Recommended future role:** Universal edge gateway and perimeter security gateway.

---

### 10. Invoice
- **Service:** Invoice
- **Purpose:** Invoicing engine for merchants: itemized bill generation, GST and discount calculation, payment tracking, and PDF document generation via iText.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, Redis, Eureka Client, iText7.
- **Port:** `8099`
- **Database:** MySQL (`invoice_db`), tables: `invoices`, `invoice_line_items`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `mysql-connector-j`, `spring-cloud-starter-netflix-eureka-client`, `itext-core`, `lombok`.
- **Inbound APIs:**
  - `POST /invoice/create` — Create invoice
  - `GET /invoice/{invoiceId}` — Fetch invoice details
  - `GET /invoice/{invoiceId}/pdf` — Stream PDF invoice
  - `PUT /invoice/{invoiceId}/mark-paid` — Mark invoice paid
  - `POST /invoice/{invoiceId}/send` — Trigger dispatch
  - `GET /invoice/merchant/{merchantUpiId}` — Fetch merchant invoices
- **Outbound APIs:** None
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Configured in `AppConfig`.
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT enforcement.
- **Current status:** Compiles cleanly and all 5 unit tests pass.
- **Problems:** Gateway has no route to `/invoice/**`; unauthenticated endpoints.
- **Recommended future role:** Merchant billing and automated PDF invoice generation service.

---

### 11. Kyc
- **Service:** Kyc
- **Purpose:** Regulatory identity verification: Aadhaar OTP verification via simulated UIDAI, PAN verification via simulated NSDL, Face Match selfie verification, and AES-256-GCM document encryption.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, Redis, Eureka Client.
- **Port:** `8094`
- **Database:** MySQL (`kyc_db`), tables: `kyc_records`, `kyc_documents`, `kyc_audit_logs`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `mysql-connector-j`, `spring-cloud-starter-netflix-eureka-client`, `lombok`.
- **Inbound APIs:**
  - `POST /api/v1/kyc/initiate-aadhaar` — Send Aadhaar OTP
  - `POST /api/v1/kyc/verify-aadhaar` — Verify Aadhaar OTP (AES encrypted)
  - `POST /api/v1/kyc/verify-pan` — Verify PAN & upgrade to Level 1
  - `POST /api/v1/kyc/face-match` — Verify face match & upgrade to Level 2
  - `GET /api/v1/kyc/status/{userId}` — Query user KYC tier and limits
  - `GET /api/v1/kyc/audit-logs/{userId}` — Retrieve verification history
- **Outbound APIs:** None
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** `UidaiService` caches Aadhaar OTPs with 10-minute TTL.
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT enforcement.
- **Current status:** Compiles cleanly and all 6 unit tests pass.
- **Problems:** Gateway has no route to port 8094; path prefix `/api/v1/kyc` deviates from general standard; mock API keys hardcoded as default values (`SECRET DETECTED — VALUE REDACTED`).
- **Recommended future role:** Core identity verification and user tier management service.

---

### 12. Loan
- **Service:** Loan
- **Purpose:** Micro-lending / BNPL (Buy Now Pay Later), credit scoring evaluation, EMI schedule calculation, loan approval/disbursement, and EMI collection via NPCI.
- **Technology:** Spring Boot 3.3.2, Java 17, Spring Data JPA, MySQL, OpenFeign, Eureka Client.
- **Port:** `8102`
- **Database:** MySQL (`loan_db`), tables: `loan_applications`, `loan_repayments`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:**
  - `POST /loan/apply` — Apply for micro-loan
  - `POST /loan/{loanId}/approve` — Approve loan
  - `POST /loan/{loanId}/disburse` — Disburse funds
  - `POST /loan/{loanId}/repay` — Collect EMI repayment
  - `GET /loan/{loanId}` — View loan status
  - `GET /loan/user/{userId}` — List user loans
  - `GET /loan/{loanId}/schedule` — View EMI repayment schedule
- **Outbound APIs:**
  - `NpciServiceClient` (Feign -> `npci-integration-service`): `POST /npci/initiate-transaction`
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** UNVERIFIED (None)
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT enforcement.
- **Current status:** Compiles cleanly and all 11 unit tests pass.
- **Problems:** Gateway has no route to `/loan/**`; dependent on broken NPCI service.
- **Recommended future role:** Credit line and BNPL loan lifecycle management service.

---

### 13. Merchant
- **Service:** Merchant
- **Purpose:** Merchant profile onboarding, store configuration, static QR code generation (base64 PNG), and dynamic QR generation.
- **Technology:** Spring Boot 3.2.5, Java 17, Spring Data JPA, MySQL, Eureka Client, ZXing.
- **Port:** `8085`
- **Database:** MySQL (`upi_merchant_db`), tables: `merchants`, `merchant_qr_codes`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `mysql-connector-j`, `spring-cloud-starter-netflix-eureka-client`, `core` (ZXing), `javase` (ZXing), `lombok`.
- **Inbound APIs:**
  - `POST /merchant/register` — Merchant onboarding
  - `GET /merchant/qr/{merchantId}` — Static QR code
  - `POST /merchant/qr/dynamic` — Dynamic amount-tagged QR code
  - `GET /merchant/{id}` — Merchant profile
- **Outbound APIs:** None
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** UNVERIFIED (None)
- **Authentication:** `SecurityConfig` specifies `.anyRequest().permitAll()`.
- **Current status:** Compiles cleanly and contextLoads test passes.
- **Problems:** Zero unit tests; unauthenticated endpoints allow anyone to register merchants or inspect data.
- **Recommended future role:** Merchant account and point-of-sale QR management service.

---

### 14. NPCI
- **Service:** NPCI
- **Purpose:** Simulates the National Payments Corporation of India (NPCI) UPI Switch: transaction routing, mandate management, UPI AutoPay, refunds, and HMAC payload signing.
- **Technology:** Spring Boot 3.5.0, Java 17, Spring Data JPA, MySQL, Redis, OpenFeign, Eureka Client.
- **Port:** `8090`
- **Database:** MySQL (`npci_db`), tables: `refund_records`, `upi_mandated`, `upi_transactions`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:**
  - `POST /npci/initiate-transaction` — Core UPI transaction routing
  - `GET /npci/check-status/{transactionId}` — Status inquiry
  - `POST /npci/refund` — Issue transaction refund
  - `GET /npci/refund/{refundId}` — Check refund status
  - `POST /npci/mandate-create` — Register recurring mandate
  - `PUT /npci/mandate/{mandateId}/pause` — Pause mandate
  - `PUT /npci/mandate/{mandateId}/revoke` — Revoke mandate
  - `GET /npci/mandate/user/{userUpiId}` — Fetch active mandates for user
- **Outbound APIs:**
  - `FraudServiceClient` (Feign -> `fraud-service`): `POST /fraud/internal/check` *(Target endpoint missing; 404)*
  - `NotificationServiceClient` (Feign -> `notification-service`): `POST /notification/internal/payment` *(Target endpoint missing; 404)*
  - `WalletServiceClient` (Feign -> `wallet-service`): `POST /wallet/internal/debit`, `POST /wallet/internal/credit` *(Target endpoints missing; 404)*
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Idempotency checks and transaction locking in `NpciTransactionService`.
- **Authentication:** `SecurityConfig` specifies `.anyRequest().permitAll()`.
- **Current status:** **COMPILATION BROKEN** (`EXIT 1`). `NpciTransactionService.java:240` fails on missing symbol `TransactionStatus`; `GlobalExceptionHandler.java` references undeclared exception classes (`DuplicateTransactionException`, `TransactionLimitExceededException`, `DailyLimitExceededException`, `NpciCommunicationException`).
- **Problems:** Compilation failure; all 3 Feign clients call endpoints that do not exist; fallback logic hides 404 failures; Gateway has no route.
- **Recommended future role:** Fix compiler issues and Feign contracts; serve as mock national UPI switch.

---

### 15. Notification
- **Service:** Notification
- **Purpose:** Event-driven notification dispatch service consuming Kafka topics to send transactional emails (Thymeleaf HTML) and SMS messages.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Kafka, Spring Mail, Thymeleaf, Eureka Client, RestTemplate.
- **Port:** `8088`
- **Database:** None
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-mail`, `spring-boot-starter-thymeleaf`, `spring-kafka`, `spring-cloud-starter-netflix-eureka-client`, `lombok`.
- **Inbound APIs:**
  - `GET /notifications/health` — Health check
  - `GET /notifications/status` — Status
- **Outbound APIs:**
  - `AuthServiceClient` (RestTemplate -> `http://auth-service/auth`): `GET /users/{userId}`, `GET /users/upi/{upiId}`
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** `NotificationKafkaConsumer` listens to:
  - `payment_completed` (group: `notification-completed-group`)
  - `payment_failed` (group: `notification-failed-group`)
  - `sync_completed` (group: `notification-sync-group`)
- **Redis:** UNVERIFIED (None)
- **Authentication:** `SecurityConfig` specifies `.anyRequest().permitAll()`.
- **Current status:** Compiles cleanly and contextLoads test passes.
- **Problems:** Port discrepancy (8088 vs 8089 in docs); lacks REST endpoint `POST /notification/internal/payment` expected by `NPCI` and `Dispute` Feign clients; zero unit tests.
- **Recommended future role:** Centralized event-driven notification dispatcher.

---

### 16. Payment
- **Service:** Payment
- **Purpose:** Core payment initiation, idempotency enforcement, digital signature creation (RSA), wallet balance verification, offline payment queuing, and Kafka event publishing.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, Spring Kafka, Resilience4j, OpenFeign, Eureka Client.
- **Port:** `8083`
- **Database:** MySQL (`upi_payment_db`), table: `payments`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-kafka`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-circuitbreaker-resilience4j`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:**
  - `POST /payment/pay` — Online payment initiation
  - `POST /payment/offline-pay` — Queue offline signed transaction
  - `POST /payment/verify` — Validate payment signature
  - `GET /payment/{paymentId}` — Fetch payment by ID
  - `GET /payment/history` — Paginated payment history
  - `GET /payment/pending-sync` — List pending offline payments
- **Outbound APIs:**
  - `WalletServiceClient` (Feign -> `wallet-service`): `GET /wallet/balance/{userId}`, `POST /wallet/debit`, `POST /wallet/credit`, `POST /wallet/freeze`, `POST /wallet/release`
- **Kafka producer:** `PaymentKafkaProducer` publishes to:
  - `payment_initiated`
  - `payment_completed`
  - `payment_failed`
- **Kafka consumer:** `PaymentKafkaConsumer` listens to:
  - `fraud_detected` (group: `payment-fraud-group`)
- **Redis:** UNVERIFIED (None)
- **Authentication:** `SecurityConfig` specifies `.anyRequest().authenticated()`.
- **Current status:** Compiles cleanly and contextLoads test passes.
- **Problems:** `PaymentService.java:89` hardcodes `.fraudStatus(Payment.FraudStatus.SAFE)` and bypasses Fraud service; `WalletServiceClient` calls `/wallet/freeze` and `/wallet/release` which do not exist in `WalletController`; zero unit tests.
- **Recommended future role:** Core payment workflow orchestrator.

---

### 17. Payroll
- **Service:** Payroll
- **Purpose:** Enterprise payroll disbursements, statutory deductions (PF cap ₹1,800, ESI threshold ₹21,000), batch validation, salary slip generation, and payment via BankGateway and NPCI.
- **Technology:** Spring Boot 3.3.2, Java 17, Spring Data JPA, MySQL, OpenFeign, Eureka Client, iText7.
- **Port:** `8104`
- **Database:** MySQL (`payroll_db`), tables: `payroll_batches`, `employee_payments`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `itext-core`, `lombok`.
- **Inbound APIs:**
  - `POST /payroll/batch/create` — Create payroll batch
  - `POST /payroll/batch/{id}/validate` — Validate batch
  - `POST /payroll/batch/{id}/process` — Process disbursements
  - `GET /payroll/batch/{id}` — Batch details
  - `GET /payroll/slip/{paymentId}/pdf` — Download salary slip PDF
- **Outbound APIs:**
  - `BankGatewayServiceClient` (Feign -> `bank-gateway-service`): `GET /bank/upi/resolve/{upiHandle}`
  - `NpciServiceClient` (Feign -> `npci-integration-service`): `POST /npci/initiate-transaction`
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** UNVERIFIED (None)
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT enforcement.
- **Current status:** Compiles cleanly and all 4 unit tests pass.
- **Problems:** Downstream service does not validate user tokens; calls BankGateway which currently fails compilation.
- **Recommended future role:** Corporate disbursements and bulk payroll processing.

---

### 18. Reconciliation
- **Service:** Reconciliation
- **Purpose:** End-of-Day financial reconciliation: compares internal transactions against external bank statements, flags discrepancies, and exports audit reports to Excel (Apache POI).
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, OpenFeign, Apache POI, Quartz Scheduler, Eureka Client.
- **Port:** `8093`
- **Database:** MySQL (`reconciliation_db`), tables: `reconciliation_reports`, `reconciliation_discrepancies`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `poi-ooxml`, `lombok`.
- **Inbound APIs:**
  - `POST /reconciliation/run` — Run reconciliation for date
  - `GET /reconciliation/report/{reportId}` — View report
  - `GET /reconciliation/report/{reportId}/discrepancies` — View discrepancies
  - `GET /reconciliation/report/{reportId}/summary` — Summary metrics
  - `GET /reconciliation/discrepancies/unresolved` — List unresolved discrepancies
  - `PUT /reconciliation/discrepancy/{discrepancyId}/resolve` — Mark discrepancy resolved
  - `GET /reconciliation/report/{reportId}/export` — Export Excel (.xlsx) file
- **Outbound APIs:**
  - `NpciServiceClient` (Feign -> `npci-integration-service`): `GET /npci/internal/status/{transactionId}` *(Mismatch! 404)*
  - `TransactionServiceClient` (Feign -> `transaction-service`): `GET /transaction/internal/by-date` *(Mismatch! 404)*
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Configured in `application.yml`.
- **Authentication:** `SecurityConfig` specifies `.anyRequest().permitAll()`.
- **Current status:** **COMPILATION BROKEN** (`EXIT 1`). `ReconciliationService.java:88` throws undefined `SettlementProcessingException`; line 158 calls undeclared `discrepancyRepo.findByDiscrepancyId(...)`.
- **Problems:** Build fails; generates mock bank statements instead of ingesting real bank feeds; Feign clients point to non-existent endpoints; Gateway has no route.
- **Recommended future role:** Daily ledger reconciliation and financial audit engine.

---

### 19. Referral
- **Service:** Referral
- **Purpose:** Referral code generation, referee signup tracking, device fraud prevention, and referral rewards issuance via Rewards service.
- **Technology:** Spring Boot 3.3.2, Java 17, Spring Data JPA, MySQL, Redis, OpenFeign, Eureka Client.
- **Port:** `8103`
- **Database:** MySQL (`referral_db`), tables: `referral_codes`, `referral_records`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:**
  - `POST /referral/code/generate` — Generate referral code
  - `POST /referral/track` — Track signup
  - `POST /referral/qualify` — Qualify referral upon first transaction
  - `GET /referral/stats/{userId}` — Referral stats
  - `GET /referral/leaderboard` — Leaderboard
  - `POST /referral/credit/{referralId}` — Payout referral bonus
- **Outbound APIs:**
  - `RewardsServiceClient` (Feign -> `rewards-service`): `POST /rewards/referral/apply` *(Target service lacks controller)*
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** `ReferralService` caches device fingerprints to prevent referral abuse.
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT enforcement.
- **Current status:** Compiles cleanly and all 12 unit tests pass.
- **Problems:** Feign client targets `rewards-service` which has no controllers and no main application class; Gateway has no route.
- **Recommended future role:** Customer acquisition and growth campaign management service.

---

### 20. Rewards
- **Service:** Rewards
- **Purpose:** User loyalty reward points, cashback offers, point crediting, point redemption, and ledger management.
- **Technology:** Spring Boot 3.3.2, Java 17, Spring Data JPA, MySQL, Redis, Eureka Client.
- **Port:** `8101`
- **Database:** MySQL (`rewards_db`), tables: `offers`, `reward_ledger`, `user_reward_accounts`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:** UNVERIFIED (None — No REST controllers exist).
- **Outbound APIs:** None
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** `RewardsService` caches user balances.
- **Authentication:** UNVERIFIED (No security configuration).
- **Current status:** **INCOMPLETE / NON-RUNNABLE**. Passes `testCompile` only because it is a headless library (entities, repositories, services), but **HAS NO `@SpringBootApplication` MAIN CLASS, NO CONTROLLERS, AND NO TESTS**.
- **Problems:** Cannot boot as a standalone microservice; `Referral` calls `/rewards/referral/apply` which does not exist anywhere.
- **Recommended future role:** Add application main class and REST controllers to make it an active loyalty rewards microservice.

---

### 21. Risk
- **Service:** Risk
- **Purpose:** Multi-signal real-time risk scoring engine evaluating device fingerprint (SHA-256), location anomalies (city checking), timing patterns, and transaction amounts (3x user average) with rolling user profiles.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, Redis, Eureka Client.
- **Port:** `8096`
- **Database:** MySQL (`risk_db`), tables: `risk_profiles`, `risk_scoring_results`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `mysql-connector-j`, `spring-cloud-starter-netflix-eureka-client`, `commons-codec`, `lombok`.
- **Inbound APIs:**
  - `POST /risk/score` — Calculate composite risk score (0.0 to 1.0)
  - `GET /risk/profile/{userId}` — Fetch risk profile
  - `GET /risk/history/{userId}` — View past scoring decisions
- **Outbound APIs:** None
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Configured in `AppConfig`.
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT enforcement.
- **Current status:** Compiles, but **TEST SUITE FAILS** (`EXIT 1`). Floating point precision failure in `RiskScoringServiceTest.testNewDeviceBoostTriggered`: `expected: <0.3> but was: <0.30000000000000004>`.
- **Problems:** Test assertion fails; service is completely **ORPHANED**: Gateway has no route to port 8096, and no other service invokes it; overlaps with Fraud and Aml.
- **Recommended future role:** Primary real-time behavioral risk scoring engine for SentinelX.

---

### 22. Settlement
- **Service:** Settlement
- **Purpose:** Daily batch merchant settlement: aggregates completed transactions, calculates platform fees and GST, creates settlement batches, and executes mock bank transfers via BankGateway.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, OpenFeign, Quartz Scheduler, Eureka Client.
- **Port:** `8092`
- **Database:** MySQL (`settlement_db`), tables: `merchant_settlements`, `settlement_batches`, `settlement_transactions`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-boot-starter-quartz`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:**
  - `POST /settlement/run` — Manually trigger daily batch settlement
  - `GET /settlement/batch/{batchId}` — View batch details
  - `GET /settlement/batch/{batchId}/report` — Summary report
  - `GET /settlement/merchant/{merchantUpiId}` — Merchant settlement history
  - `GET /settlement/batch/date/{date}` — Fetch batch by date
- **Outbound APIs:**
  - `BankGatewayClient` (Feign -> `bank-gateway-service`): `GET /bank/accounts/primary/{merchantUpiId}` *(Mismatch! 404)*
  - `TransactionServiceClient` (Feign -> `transaction-service`): `GET /transaction/internal/unsettled`, `POST /transaction/internal/mark-settled` *(Mismatch! 404)*
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Configured in `application.yml`.
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT enforcement.
- **Current status:** Compiles, but **TEST SUITE FAILS** (`EXIT 1`). `SettlementServiceTest.handlesPartialMerchantFailure` throws `NullPointerException: Cannot read field "intCompact" because "subtrahend" is null` during `BigDecimal.subtract`.
- **Problems:** Test NPE failure; inter-service Feign contracts point to non-existent endpoints on BankGateway and Transaction services; Gateway has no route.
- **Recommended future role:** Daily merchant settlement and platform fee processing engine.

---

### 23. Subscription
- **Service:** Subscription
- **Purpose:** Manages recurring payment mandates (UPI AutoPay), scheduling automated recurring charges, dunning retry logic for failed payments, and mandate lifecycle management.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, OpenFeign, Quartz Scheduler, Eureka Client.
- **Port:** `8098`
- **Database:** MySQL (`subscription_db`), tables: `subscriptions`, `subscription_payment_attempts`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-boot-starter-quartz`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:**
  - `POST /subscription/create` — Create recurring subscription
  - `PUT /subscription/{subscriptionId}/pause` — Pause subscription
  - `PUT /subscription/{subscriptionId}/resume` — Resume subscription
  - `PUT /subscription/{subscriptionId}/cancel` — Cancel subscription
  - `GET /subscription/{subscriptionId}` — View subscription
  - `GET /subscription/user/{userId}` — List user subscriptions
  - `GET /subscription/{subscriptionId}/attempts` — View execution history
  - `POST /subscription/execute-scheduled` — Trigger scheduled charge execution
- **Outbound APIs:**
  - `NpciServiceClient` (Feign -> `npci-integration-service`): `POST /npci/mandate-create`, `PUT /npci/mandate/{mandateId}/pause`, `PUT /npci/mandate/{mandateId}/revoke`, `POST /npci/initiate-transaction`
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** Configured in `AppConfig`.
- **Authentication:** `SecurityConfig` permits actuator and swagger; no JWT enforcement.
- **Current status:** Compiles cleanly and all 6 unit tests pass.
- **Problems:** Gateway has no route to `/subscription/**`; dependent on NPCI service which currently fails compilation.
- **Recommended future role:** UPI AutoPay and recurring subscription billing.

---

### 24. Sync
- **Service:** Sync
- **Purpose:** Offline payment synchronization when client reconnects to the network, synchronizing offline signed payments with payment-service and emitting Kafka events.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, Spring Kafka, OpenFeign, Eureka Client.
- **Port:** `8089`
- **Database:** MySQL (`upi_sync_db`), table: `sync_records`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-kafka`, `spring-cloud-starter-openfeign`, `spring-cloud-starter-netflix-eureka-client`, `mysql-connector-j`, `lombok`.
- **Inbound APIs:**
  - `POST /sync/process` (Header: `X-User-Id`) — Process offline pending payments
  - `GET /sync/status` — Service health and status
- **Outbound APIs:**
  - `PaymentServiceClient` (Feign -> `payment-service`): `GET /payment/pending-sync` (Sends request body with GET), `POST /payment/offline-pay`
- **Kafka producer:** Publishes to topics:
  - `payment_initiated`
  - `sync_completed`
- **Kafka consumer:** UNVERIFIED (None)
- **Redis:** UNVERIFIED (None)
- **Authentication:** `SecurityConfig` specifies `.anyRequest().permitAll()`.
- **Current status:** Compiles cleanly and contextLoads test passes.
- **Problems:** Controller package misspelled `com.syncService.Sync.contoller`; port 8089 conflicts with Notification documentation; `PaymentServiceClient` uses GET with request body (`getPendingSyncPayments`).
- **Recommended future role:** Resilient offline payment synchronization coordinator.

---

### 25. Transaction
- **Service:** Transaction
- **Purpose:** Central immutable transaction ledger, merchant transaction reporting, user spending summaries, and polyglot persistence (MySQL ledger + MongoDB audit/payment logs).
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, Spring Data MongoDB, Spring Kafka, Eureka Client.
- **Port:** `8084`
- **Database:** Polyglot: MySQL (`upi_transaction_db`, table: `transactions`) + MongoDB (`upi_transaction_audit_db`, collections: `audit_logs`, `payment_logs`). Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-mongodb`, `spring-kafka`, `mysql-connector-j`, `spring-cloud-starter-netflix-eureka-client`, `lombok`.
- **Inbound APIs:**
  - `GET /transactions/user/{id}` — User paginated transaction history
  - `GET /transactions/merchant/{id}` — Merchant transaction history
  - `GET /transactions/spend-summary/{userId}` — Total spend summary
  - `GET /transactions/debit-between` — Query debit transactions in time range
- **Outbound APIs:** None
- **Kafka producer:** UNVERIFIED (None)
- **Kafka consumer:** `TransactionKafkaConsumer` listens to topics:
  - `payment_completed` (group: `transaction-completed-group`)
  - `payment_failed` (group: `transaction-failed-group`)
  - `sync_completed` (group: `transaction-sync-group`)
- **Redis:** UNVERIFIED (None)
- **Authentication:** `SecurityConfig` specifies `.anyRequest().permitAll()`.
- **Current status:** **COMPILATION BROKEN** (`EXIT 1`). Syntax error in `TransactionRepository.java:49`: missing `@` symbol before `org.springframework.data.repository.query.Param("from")`.
- **Problems:** Syntax error breaks build; lacks internal endpoints required by Settlement (`/transaction/internal/unsettled`, `/transaction/internal/mark-settled`) and Reconciliation (`/transaction/internal/by-date`); zero unit tests.
- **Recommended future role:** Central immutable financial transaction ledger and audit store.

---

### 26. Wallet
- **Service:** Wallet
- **Purpose:** Digital wallet balance management, top-ups/deposits, real-time debit and credit operations, and Kafka event consumption.
- **Technology:** Spring Boot 3.3.5, Java 17, Spring Data JPA, MySQL, Redis, Spring Kafka, Eureka Client.
- **Port:** `8082`
- **Database:** MySQL (`upi_wallet_db`), tables: `wallets`, `wallet_transactions`. Hibernate DDL-Auto: `update`.
- **Dependencies:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-kafka`, `mysql-connector-j`, `spring-cloud-starter-netflix-eureka-client`, `lombok`.
- **Inbound APIs:**
  - `GET /wallet/balance/{userId}` — Fetch available wallet balance (cached in Redis)
  - `POST /wallet/create` (Header: `X-User-Id`) — Create wallet for user
  - `POST /wallet/add-money` — Bank top-up into wallet
  - `POST /wallet/debit` — Debit wallet balance
  - `POST /wallet/credit` — Credit wallet balance
  - `GET /wallet/info/{userId}` — Wallet profile details
- **Outbound APIs:** None
- **Kafka producer:** Publishes to topics:
  - `payment_completed`
  - `payment_failed`
- **Kafka consumer:** `WalletKafkaConsumer` listens to topic:
  - `payment_initiated` (group: `wallet_payment_group`)
- **Redis:** Redis cache for fast balance lookups.
- **Authentication:** `SecurityConfig` specifies `.anyRequest().permitAll()`.
- **Current status:** Compiles cleanly and contextLoads test passes.
- **Problems:** Zero unit tests; missing endpoints called by Payment (`/wallet/freeze`, `/wallet/release`) and NPCI (`/wallet/internal/debit`, `/wallet/internal/credit`); completely unauthenticated endpoints.
- **Recommended future role:** Core balance ledger and wallet account authority.
