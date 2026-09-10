# UPI MESH — CURRENT DEPENDENCY GRAPH

> **Audit Timestamp:** September 2026  
> **Target Repository:** `d:\UpiMesh`  
> **Phase:** 01 — Existing System Audit & Architecture Recovery  
> **Standard:** Only relationships confirmed directly from code annotations (`@FeignClient`, `@KafkaListener`, `kafkaTemplate.send()`, `RestTemplate`, Gateway routes, and Eureka client configs) are documented.

---

## 1. Top-Level Architectural Graph

```
                                  [ Client / Browser ]
                                           │
                                           ▼
                                [ API Gateway (8080) ]
                                           │
       ┌───────────┬──────────────┬────────┼─────────────┬─────────────┬───────────┐
       ▼           ▼              ▼        ▼             ▼             ▼           ▼
    [ Auth ]   [ Wallet ]    [ Payment ] [ Txn ]    [ Merchant ]  [ Payroll ]  [ AI ]
     (8081)      (8082)        (8083)    (8084)        (8085)       (8104)     (8087)
       │           ▲              │        ▲             │             │           │
       │           │              │        │             │             ▼           ▼
       │     (Feign/Kafka)        ▼        │             │          [ NPCI ]   [ OpenAI ]
       │           │     (Kafka: payment_  │             │           (8090)
       │           │        initiated)     │             │             ▲
       │           └──────────────┼────────┤             │             │
       │                          ▼        │             │     ┌───────┴───────┐
       │                 [ Kafka Message ] │             │     │ (Feign)       │
       │                 [     Broker    ] │             │     ▼               ▼
       │                 [    (9092)     ] │             │ [ Dispute ]   [ Subscriptions ]
       │                          │        │             │  (8100)          (8098)
       │                          ▼        │             │
       │               (Topics: payment_   │             │
       │                completed/failed)  │             │
       │                          │        │             │
       │                          ├────────┘             │
       │                          ▼                      │
       │                   [ Notification ]              │
       │                       (8088)                    │
       │                          │                      │
       └──────────────────────────┘                      │
            (RestTemplate: /auth/users)                  │
                                                         │
─────────────────────────────────────────────────────────┼─────────────────────────────
ORPHANED SERVICES (No Gateway Route & No Inbound Feign): │
                                                         ▼
 [ AML (8095) ]    [ Risk (8096) ]    [ KYC (8094) ]    [ Invoice (8099) ]
```

---

## 2. Verified Inbound Gateway Routing

Derived strictly from `Gateway/Gateway/src/main/resources/application.yml`:

| Gateway Route Predicate | Target Service (`lb://`) | Destination Port | Gateway Filters |
|---|---|---|---|
| `/auth/**` | `auth-service` | `8081` | RateLimiter (20 req/s), CircuitBreaker |
| `/wallet/**` | `wallet-service` | `8082` | RateLimiter (50 req/s), CircuitBreaker, AddHeader (`X-Gateway-Source`) |
| `/payment/**` | `payment-service` | `8083` | RateLimiter (30 req/s), CircuitBreaker |
| `/transactions/**` | `transaction-service` | `8084` | RateLimiter (50 req/s) |
| `/merchant/**` | `merchant-service` | `8085` | None |
| `/fraud/**` | `fraud-service` | `8086` | None |
| `/ai/**` | `ai-service` | `8087` | RateLimiter (5 req/s) |
| `/sync/**` | `sync-service` | `8089` | None |
| `/payroll/**` | `payroll-service` | `8104` | None |
| `/analytics/**` | `analytics-service` | `8105` | None |

> **Unrouted Services Alert:** The following 14 services have **NO** route entries in `Gateway`:  
> `BankGateway (8091)`, `Settlement (8092)`, `Reconciliation (8093)`, `Kyc (8094)`, `Aml (8095)`, `Risk (8096)`, `Subscription (8098)`, `Invoice (8099)`, `Dispute (8100)`, `Rewards (8101)`, `Loan (8102)`, `Referral (8103)`, `NPCI (8090)`, `Notification (8088)`.

---

## 3. Synchronous Inter-Service REST Dependencies

Derived from `@FeignClient` interfaces and `RestTemplate` beans:

### Verified & Contract-Matched
```
Payment (8083)
  └──> (Feign: WalletServiceClient) ──> Wallet (8082)
         ├── GET  /wallet/balance/{userId}
         ├── POST /wallet/debit
         └── POST /wallet/credit

Sync (8089)
  └──> (Feign: PaymentServiceClient) ──> Payment (8083)
         ├── GET  /payment/pending-sync
         └── POST /payment/offline-pay

Notification (8088)
  └──> (RestTemplate: AuthServiceClient) ──> Auth (8081)
         ├── GET  /auth/users/{userId}
         └── GET  /auth/users/upi/{upiId}

Loan (8102)
  └──> (Feign: NpciServiceClient) ──> NPCI (8090)
         └── POST /npci/initiate-transaction

Payroll (8104)
  ├──> (Feign: BankGatewayServiceClient) ──> BankGateway (8091)
  │      └── GET  /bank/upi/resolve/{upiHandle}
  └──> (Feign: NpciServiceClient) ──> NPCI (8090)
         └── POST /npci/initiate-transaction

Subscription (8098)
  └──> (Feign: NpciServiceClient) ──> NPCI (8090)
         ├── POST /npci/mandate-create
         ├── PUT  /npci/mandate/{mandateId}/pause
         ├── PUT  /npci/mandate/{mandateId}/revoke
         └── POST /npci/initiate-transaction

Dispute (8100)
  └──> (Feign: NpciServiceClient) ──> NPCI (8090)
         ├── GET  /npci/check-status/{transactionId}
         └── POST /npci/refund

Analytics (8105)
  ├──> (Feign: AuthServiceClient) ──> Auth (8081)
  │      ├── GET  /auth/users/count/new
  │      └── GET  /auth/users/count/active
  └──> (Feign: TransactionServiceClient) ──> Transaction (8084)
         └── GET  /transactions/debit-between
```

### Broken / Contract Mismatched (Produce 404 Runtime Errors)
```
NPCI (8090)
  ├──> (Feign: FraudServiceClient) ──> Fraud (8086)
  │      └── POST /fraud/internal/check ──❌ [404: FraudController only has /fraud/check]
  ├──> (Feign: NotificationServiceClient) ──> Notification (8088)
  │      └── POST /notification/internal/payment ──❌ [404: Endpoint missing in NotificationController]
  └──> (Feign: WalletServiceClient) ──> Wallet (8082)
         ├── POST /wallet/internal/debit ──❌ [404: Endpoint missing in WalletController]
         └── POST /wallet/internal/credit ──❌ [404: Endpoint missing in WalletController]

Dispute (8100)
  └──> (Feign: NotificationServiceClient) ──> Notification (8088)
         └── POST /notification/internal/payment ──❌ [404: Endpoint missing in NotificationController]

Payment (8083)
  └──> (Feign: WalletServiceClient) ──> Wallet (8082)
         ├── POST /wallet/freeze ──❌ [404: Endpoint missing in WalletController]
         └── POST /wallet/release ──❌ [404: Endpoint missing in WalletController]

Referral (8103)
  └──> (Feign: RewardsServiceClient) ──> Rewards (8101)
         └── POST /rewards/referral/apply ──❌ [404: Rewards has no controller or main class]

Settlement (8092)
  ├──> (Feign: BankGatewayClient) ──> BankGateway (8091)
  │      └── GET  /bank/accounts/primary/{merchantUpiId} ──❌ [404: Endpoint missing in BankGatewayController]
  └──> (Feign: TransactionServiceClient) ──> Transaction (8084)
         ├── GET  /transaction/internal/unsettled ──❌ [404: Endpoint missing in TransactionController]
         └── POST /transaction/internal/mark-settled ──❌ [404: Endpoint missing in TransactionController]

Reconciliation (8093)
  ├──> (Feign: NpciServiceClient) ──> NPCI (8090)
  │      └── GET  /npci/internal/status/{transactionId} ──❌ [404: NpciController only has /npci/check-status/{id}]
  └──> (Feign: TransactionServiceClient) ──> Transaction (8084)
         └── GET  /transaction/internal/by-date ──❌ [404: Endpoint missing in TransactionController]
```

---

## 4. Asynchronous Kafka Event Flows

```
[Payment (8083)]  ───(publishes)───┐
                                    ▼
[Sync (8089)]     ───(publishes)───> [ Topic: payment_initiated ]
                                                │
                                                ▼ (consumes)
                                         [ Wallet (8082) ]
                                                │
                       ┌────────────────────────┴────────────────────────┐
                       ▼ (debit/credit success)                          ▼ (debit/credit failure)
            [ Topic: payment_completed ]                        [ Topic: payment_failed ]
                       │                                                 │
        ┌──────────────┴──────────────┐                   ┌──────────────┴──────────────┐
        ▼ (consumes)                  ▼ (consumes)        ▼ (consumes)                  ▼ (consumes)
[ Transaction (8084) ]    [ Notification (8088) ]   [ Transaction (8084) ]    [ Notification (8088) ]
(Writes to MySQL/Mongo)   (Sends Email/SMS)         (Records Failure)         (Sends Alert)
```

```
[Sync (8089)] ────(publishes)────> [ Topic: sync_completed ]
                                                │
                                 ┌──────────────┴──────────────┐
                                 ▼ (consumes)                  ▼ (consumes)
                      [ Transaction (8084) ]          [ Notification (8088) ]
```

```
[ UNPRODUCED TOPIC ] ────────────> [ Topic: fraud_detected ]
                                                │
                                                ▼ (consumes)
                                        [ Payment (8083) ]
```

> **Pipeline Disconnection Finding:** In `PaymentService.java:89`, the payment transaction is created with `.fraudStatus(Payment.FraudStatus.SAFE)` and published directly to `payment_initiated`. Neither `Fraud`, `Risk`, nor `Aml` consumes `payment_initiated` or publishes to `fraud_detected`. The fraud screening subsystem is entirely bypassed during online payment execution.

---

## 5. Completely Orphaned Services

The following microservices have:
1. No routing configuration in API Gateway (`application.yml`)
2. No incoming Feign or REST calls from any other microservice in the repository
3. No incoming Kafka message consumers

| Service Name | Port | Description |
|---|---|---|
| **Aml** | `8095` | Anti-Money Laundering screening & watchlist checks |
| **Risk** | `8096` | Behavioral & heuristic risk scoring engine |
| **Kyc** | `8094` | Aadhaar/PAN identity validation & tier limits |
| **Invoice** | `8099` | Merchant billing and PDF generation |

---

## 6. Service Discovery Map (Eureka)

All 25 application services register as Eureka clients with `Eureka` (`eureka-server` on port `8761`):

```
                                [ Eureka Server (8761) ]
                                           │
  ┌───────────────┬───────────────┬────────┼───────────────┬───────────────┬───────────────┐
  ▼               ▼               ▼        ▼               ▼               ▼               ▼
ai-service   aml-service    analytics-   auth-       bank-gateway-    dispute-        fraud-
(8087)       (8095)         service      service     service          service         service
                            (8105)       (8081)      (8091)           (8100)          (8086)
  │               │               │        │               │               │               │
  ▼               ▼               ▼        ▼               ▼               ▼               ▼
api-gateway  invoice-       kyc-service  loan-       merchant-        npci-           notification-
(8080)       service        (8094)       service     service          integration-    service
             (8099)                      (8102)      (8085)           service (8090)  (8088)
  │               │               │        │               │               │               │
  ▼               ▼               ▼        ▼               ▼               ▼               ▼
payment-     payroll-       reconcil-    referral-   rewards-         risk-scoring-   settlement-
service      service        iation-svc   service     service          service         service
(8083)       (8104)         (8093)       (8103)      (8101)           (8096)          (8092)
  │               │               │
  ▼               ▼               ▼
subscription- sync-service   transaction-
service       (8089)         service
(8098)                       (8084)
```
