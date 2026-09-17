# UPI Mesh & SentinelX — Enterprise Security, Cryptography & Compliance Blueprint

> **Author:** 15+ Years Principal Fintech & Security Architect  
> **Classification:** Enterprise Security Architecture & Compliance Standard  
> **Target Compliance:** RBI Master Directions, PMLA 2002, DPDP Act 2023, PCI-DSS Level 1  

---

## 1. Zero-Trust Security Philosophy

The UPI Mesh platform is engineered around the core tenets of **Zero-Trust Architecture (NIST SP 800-207)**:
1. **Never Trust, Always Verify:** Every network request—whether originating from an external mobile client or an internal microservice—must be explicitly authenticated and authorized.
2. **Least Privilege Access:** Services and human operators are restricted to the minimal access rights necessary to perform their business function.
3. **Assume Breach:** Security controls are layered in defense-in-depth: perimeter filtering, field-level encryption, immutable audit trails, and cryptographically verified payload integrity.

```
       [ Public Internet ]
               │
               ▼  (TLS 1.3 / HTTPS Termination)
    ┌────────────────────────────────────────────────────────┐
    │  Spring Cloud API Gateway (Port 8080)                  │
    │  - Reactive Token Bucket Rate Limiting (Redis)         │
    │  - JWT Signature Validation & Claim Extraction         │
    │  - Header Sanitization (Strips external spoofed X-*)   │
    │  - Injects Verified Headers: X-User-Id, X-User-Roles   │
    └────────────────────────────────────────────────────────┘
               │
               ▼  (Internal Microservices Network / Private VPC)
    ┌──────────────────┬──────────────────┬──────────────────┐
    ▼                  ▼                  ▼                  ▼
 [ Auth (8081) ]  [ Payment (8083) ] [ KYC (8094) ]     [ AML (8095) ]
  BCrypt Hashing   RSA Verification   AES-256-GCM        Redis Counters
  MySQL + Redis    Kafka Streaming    Sensitive PII      Sanctions Match
```

---

## 2. Perimeter Defense & Identity Propagation

### 2.1 Edge Authentication at API Gateway
The **Spring Cloud API Gateway** acts as the solitary public ingress point. It enforces perimeter access controls using the `JwtAuthenticationFilter`:

1. **Token Validation:** Every inbound request to protected routes must provide a signed JWT Bearer token in the `Authorization` header.
2. **Signature & Expiration Verification:** The Gateway verifies the token signature using standard HMAC-SHA256 / RSA keys and validates that `exp > currentTime`.
3. **Header Sanitization & Ingress Quarantine:** To prevent client-side header injection (e.g., an attacker manually providing `X-User-Id: admin`), the Gateway strips any client-provided internal headers before routing.
4. **Header Ingress Propagation:** Upon successful validation, the Gateway injects authenticated identity headers downstream:
   - `X-User-Id`: The authenticated user's unique identifier.
   - `X-User-Email`: The user's primary email.
   - `X-User-Roles`: Authorized roles (`ROLE_USER`, `ROLE_MERCHANT`, `ROLE_ADMIN`).
   - `X-Gateway-Source`: A cryptographic internal assertion confirming the request originated from the trusted gateway.

### 2.2 Microservice Internal Interceptor
Downstream domain microservices (e.g., `Payment`, `Wallet`, `Settlement`) reject direct invocations lacking valid internal headers or the shared `internal.service-key`, ensuring that unauthenticated external calls cannot bypass the gateway perimeter.

---

## 3. Cryptographic Architecture & Primitives

The ecosystem employs distinct cryptographic algorithms tailored to each threat vector:

| Purpose | Algorithm | Key Size / Spec | Implementation |
|---|---|---|---|
| **Field-Level Data-at-Rest** | `AES/GCM/NoPadding` | 256 bits | `KycEncryptionUtil.java`, `EncryptionUtil.java` |
| **Offline Digital Signatures**| `SHA256withRSA` | 2048-bit RSA | `PaymentSignatureUtil.java` |
| **User Password Hashing** | Salted `BCrypt` | 12 rounds | `SecurityConfig.java` in `Auth` |
| **Session & Token Signing** | `HMAC-SHA256` | 256-bit Secret | `JwtUtil.java` in `Auth` & `Gateway` |
| **Device Fingerprinting** | `SHA-256` | 256-bit Digest | `RiskScoringService.java`, `ReferralService.java` |

---

### 3.1 Field-Level Encryption: AES-256-GCM (AEAD)
Standard symmetric encryption modes (such as AES-CBC) are vulnerable to chosen-ciphertext and padding oracle attacks. For regulatory PII protection (Aadhaar numbers, PAN cards, bank account details), UPI Mesh enforces **AES-GCM (Galois/Counter Mode)**:

- **Authenticated Encryption (AEAD):** GCM mode simultaneously computes an authentication tag (128-bit) alongside ciphertext.
- **Unique Initialization Vector (IV):** A 12-byte (96-bit) IV is generated per operation using `java.security.SecureRandom`. IVs are never reused under the same key.
- **Tamper Detection:** Any unauthorized alteration of ciphertext bits or the prepended IV results in an immediate `AEADBadTagException` during decryption, neutralizing bit-flipping attacks.
- **Storage Format:** 
  $$\text{Payload} = \text{Base64}\left( \text{IV}_{12\,\text{bytes}} \parallel \text{Ciphertext}_{N\,\text{bytes}} \parallel \text{AuthTag}_{16\,\text{bytes}} \right)$$

---

### 3.2 Asymmetric Non-Repudiation: RSA-2048
For offline mesh transactions where an immediate server-side validation is impossible:
1. The client device generates an asymmetric key pair during onboarding. The public key is registered with the central platform.
2. During offline payment, the client signs the canonical transaction data ($\text{sender} \parallel \text{receiver} \parallel \text{amount} \parallel \text{timestamp} \parallel \text{nonce}$) using its private key:
   $$\sigma = \text{Sign}_{K_{\text{priv}}}(\text{SHA256}(\mathcal{M}))$$
3. When connectivity is restored, the `SyncService` and `PaymentService` verify the signature using the user's stored public key. If verified, the transaction is immutable and non-repudiable; the sender cannot deny having authorized the transfer.

---

### 3.3 Credential Defense: Salted BCrypt
User passwords in the `Auth` service are never stored in plaintext. They are processed using the **BCrypt adaptive hashing function**:
- Work factor / cost parameter set to **12** ($2^{12} = 4096$ iterations).
- Automatically incorporates a cryptographically secure 128-bit per-password salt to neutralize rainbow table and offline dictionary attacks.

---

## 4. Defense-in-Depth Threat Mitigation Matrix

| Threat Vector | Attack Scenario | Platform Defense Mechanism |
|---|---|---|
| **Replay Attacks** | An attacker captures an API request and retransmits it to execute duplicate fund debits. | **Idempotency Keys & Nonces:** `Payment` and `NPCI` check idempotency tokens in Redis/MySQL with unique UUID constraints. Duplicate keys within 24h are rejected. |
| **Man-In-The-Middle (MITM)** | An adversary intercepts network traffic between the mobile client and the platform. | **TLS 1.3 Termination & RSA Signatures:** All transport is encrypted. Sensitive transaction payloads are digitally signed with RSA-2048, rendering captured payloads untamperable. |
| **Credential Stuffing & Bot Brute-Force** | Automated bots flood `/auth/login` or `/auth/verify-otp` with stolen credentials. | **Distributed Token-Bucket Rate Limiting:** Redis reactive rate limiters cap OTP attempts to 3 requests per 15 minutes per phone number, and limit gateway traffic to 20 req/s per IP. |
| **Smurfing / Structuring** | A bad actor splits a large illegal transfer into 10 smaller ₹9,500 transactions to bypass reporting limits. | **Anti-Smurfing Detection Engine:** `AmlService` runs rolling 24-hour scans detecting clustering just below ₹10,000 and ₹50,000 limits, generating automated compliance alerts. |
| **Data At Rest Theft** | An attacker gains unauthorized read access to the database dump. | **AES-256-GCM Field-Level Encryption:** Aadhaar, PAN, and bank account numbers are stored as encrypted Base64 strings. Without access to external KMS/Vault keys, database contents are unreadable. |
| **Insider Fraud & Ledger Tampering** | A rogue database administrator attempts to manually alter wallet balances. | **Dual Ledger & Polyglot Audit Trail:** Every wallet balance modification must have a corresponding immutable event log in the MySQL `wallet_transactions` table and the append-only MongoDB `audit_logs` collection. Daily 3-way reconciliation flags any discrepancy. |

---

## 5. Regulatory Compliance Alignment

```
┌───────────────────────────────────────────────────────────────────────┐
│                      REGULATORY COMPLIANCE FRAMEWORK                  │
├───────────────────┬───────────────────┬───────────────────────────────┤
│   RBI DIRECTIVES  │     PMLA 2002     │        DPDP ACT 2023          │
│ - 3-Tier KYC      │ - PEP Screening   │ - Field-Level Encryption      │
│ - e-Mandates      │ - Sanctions Match │ - Purpose Limitation          │
│ - Dispute SLAs    │ - Structuring STR │ - Audit Trail Immutability    │
└───────────────────┴───────────────────┴───────────────────────────────┘
```

### 5.1 RBI Master Directions on Prepaid Payment Instruments (PPIs)
1. **Tiered KYC Verification:**
   - **Tier 0 (Minimum Detail PPI):** Mobile number + OTP verification. Monthly limit capped at **₹10,000**.
   - **Tier 1 (Small PPI):** Verified PAN card + Aadhaar OTP via simulated UIDAI. Monthly limit elevated to **₹1,00,000**.
   - **Tier 2 (Full KYC PPI):** Biometric / Face Match verification comparing selfie against government photo. Unlocks **unlimited** transacting capabilities.
2. **UPI AutoPay / Recurring Mandates:**
   - Mandate creation requires customer explicit consent with pre-debit registration.
   - Enforces a 3-step exponential dunning retry cadence (Day 0 $\rightarrow$ Day +1 $\rightarrow$ Day +3) before automated cancellation.
3. **Dispute Resolution SLA:**
   - Mandates a customer dispute filing window of up to **90 days**.
   - Enforces a **48-hour** merchant rebuttal SLA before default customer refund arbitration.

### 5.2 Prevention of Money Laundering Act (PMLA 2002)
1. **Sanctions & PEP Screening:** Real-time fuzzy matching ($\ge 80\%$ Levenshtein similarity) against OFAC, United Nations Security Council (UNSC), and national Politically Exposed Persons registries before onboarding or processing high-value transfers.
2. **Velocity & Smurfing Controls:** Automated transaction frequency limits (10 txns/hr, 25 txns/day) and multi-transaction structuring detection for sub-threshold transfers.
3. **Suspicious Transaction Reporting (STR):** Automated risk rating and escalation workflows (`AML_ALERTS`) with audit history retained for 5+ years.

### 5.3 Digital Personal Data Protection Act (DPDP 2023)
1. **Data Minimization:** No raw biometric data or complete 12-digit Aadhaar numbers are persisted in plaintext. Aadhaar numbers are masked ($XXXX-XXXX-1234$) and the full string is encrypted via AES-256-GCM.
2. **Access Control & Purpose Limitation:** Internal service endpoints require authenticated role authorization. Direct public access to PII tables is prohibited.
3. **Audit Logging:** Every access, mutation, or verification attempt is logged with timestamp, user ID, IP address, and operation outcome in the `kyc_audit_logs` table.

### 5.4 PCI-DSS Level 1 Standards Alignment
- **Network Segmentation:** Internal microservices communicate within a dedicated virtual network (`upimesh-network` in Docker Compose / Kubernetes cluster private network).
- **Protection of Cardholder & Account Data:** Sensitive bank credentials and debit card details used in `BankGateway` are encrypted at rest with AES-256 and transmitted exclusively over TLS.
- **Vulnerability & Identity Management:** Unique user identification, role-based access control (RBAC), and session expiration after 15 minutes of inactivity.

---

## 6. Key Management & Production Deployment Runbook

In a production banking environment, encryption keys must never be stored in plaintext configuration files or environment variables.

### Key Management Blueprint (AWS KMS / HashiCorp Vault)
1. **Envelope Encryption:**
   - A **Customer Master Key (CMK)** is managed inside AWS KMS or HashiCorp Vault.
   - Ephemeral **Data Encryption Keys (DEKs)** are generated for field encryption and rotated on a 90-day cadence.
2. **Secrets Storage:**
   - Production secrets (`JWT_SECRET`, `KYC_ENCRYPTION_KEY`, `RSA_PRIVATE_KEY`) must be fetched at application bootstrap via **Spring Cloud Vault** or **AWS Secrets Manager**, mounted as in-memory secrets in Kubernetes pods.
3. **Key Rotation Lifecycle:**
   - To rotate encryption keys without downtime, the system supports dual-key decryption: data is decrypted using `KEY_VERSION_OLD` and re-encrypted using `KEY_VERSION_NEW` during scheduled maintenance migrations.

---

*Authored by Principal Fintech Systems Architect for the UPI Mesh Platform.*
