# 🚀 UPI MESH & SENTINELX — FRONTEND ARCHITECTURE & IMPLEMENTATION MASTER PROMPT

> **Document Type:** Production-Grade AI Agent Master Prompt & Engineering Specification  
> **Target Project:** `UpiMesh & SentinelX` Enterprise Distributed Fintech Platform  
> **Target Stack:** Next.js 14+ (App Router), TypeScript, Tailwind CSS, shadcn/ui, Framer Motion, GSAP, WebCrypto API, Zustand, TanStack Query  
> **Backend Footprint:** 27 Microservices (Spring Cloud Gateway `:8080`, Eureka `:8761`, 24 Spring Boot services, 1 Python FastAPI ML Engine `:8000`)  

---

## 🎯 Executive Goal & Persona

Act as a **Staff Principal Frontend Engineer & Design Technologist** with 15+ years of experience building mission-critical fintech interfaces (Stripe, Apple Pay, Razorpay, Revolut). 

Your objective is to build and elevate the **UPI Mesh & SentinelX Frontend** into a **world-class, high-security, visually breathtaking web application**. It must combine consumer-grade payment elegance (3D interactive cards, smooth physics-based animations, sticky scroll reveals, AI visual assets) with bank-grade security protocols (Zero-Trust JWT rotation, client-side RSA-2048 signing, AES-256-GCM payload encryption, real-time Kafka event streams) across all 27 microservices.

---

## 📐 System Architecture & Microservice Topology

All client requests **must route exclusively through the Spring Cloud API Gateway** (`http://localhost:8080`) or direct to the ML Engine (`http://localhost:8000`).

```
                              [ Web Client (Next.js 14+) ]
                                           │
                ┌──────────────────────────┴──────────────────────────┐
                │ TLS 1.3 / Bearer JWT / Idempotency-Key / Nonce     │
                ▼                                                     ▼
   ┌───────────────────────────────┐              ┌───────────────────────────────┐
   │ Spring Cloud Gateway (:8080)  │              │ Python FastAPI ML (:8000)     │
   │ - Redis Token Bucket Rate Lim │              │ - /predict (RF & XGBoost)     │
   │ - Header Sanitization         │              │ - /train/continuous           │
   │ - Injects X-User-Id, Roles    │              │ - /models/candidates & approve│
   └───────────────┬───────────────┘              └───────────────────────────────┘
                   │
    ┌──────────────┼──────────────────────────────┬──────────────────────────────┐
    ▼              ▼                              ▼                              ▼
[ Auth :8081 ]  [ Wallet :8082 ]               [ Payment :8083 ]              [ Transaction :8084 ]
- /auth/login   - /wallet/balance/{id}         - /payment/pay                 - /transactions/user/{id}
- /auth/otp     - /wallet/add-money            - /payment/offline-pay         - /transactions/spend-summary
- /auth/refresh - /wallet/freeze               - /payment/verify (RSA)        - MySQL + MongoDB Audit
    │              │                              │                              │
    ▼              ▼                              ▼                              ▼
[ Merchant :8085] [ Fraud :8086 ]              [ AI Service :8087 ]           [ Notification :8088 ]
- Dynamic QR    - /fraud/check                 - /ai/chat                     - Kafka Consumer
- BharatQR      - /fraud/cases & clusters      - /ai/fraud-explain            - Push / SMS / Email
    │              │                              │                              │
    ▼              ▼                              ▼                              ▼
[ Sync :8089 ]  [ NPCI Switch :8090 ]          [ Bank Gateway :8091 ]         [ Settlement :8092 ]
- Offline Mesh  - ISO-8583 Interbank           - HDFC/SBI/ICICI Strategy      - EOD Bulk Payouts
- Nonce Verify  - AutoPay Mandates             - CBS Balance & IFSC Lookup    - GST (18%) & Platform Fee
    │              │                              │                              │
    ▼              ▼                              ▼                              ▼
[ Recon :8093 ] [ KYC Service :8094 ]          [ AML Service :8095 ]          [ Risk Engine :8096 ]
- 3-Way Match   - Aadhaar OTP (AES-GCM)        - PMLA Smurfing Detection      - Sub-10ms Scoring (0-1000)
- POI Excel Exp - PAN Verify & Face Match      - OFAC/PEP Levenshtein Match   - 9 Rule Strategies
    │              │                              │                              │
    ▼              ▼                              ▼                              ▼
[ Sub :8098 ]   [ Invoice :8099 ]              [ Dispute :8100 ]              [ Rewards :8101 ]
- UPI AutoPay   - B2B GST Invoice (iText7)     - 90-Day Chargeback SLA        - Cashback & Loyalty
- Dunning Cad.  - Dynamic Payload QR           - Evidence Attachment          - Points Ledger
    │              │                              │                              │
    ▼              ▼                              ▼                              ▼
[ Loan :8102 ]  [ Referral :8103 ]             [ Payroll :8104 ]              [ Analytics :8105 ]
- BNPL & CIBIL  - Device Fingerprint SHA-256   - Bulk Salary Disbursement     - Platform GMV & TPS
- EMI Amortize  - Anti-Fraud Referral Loop     - Statutory Deductions (PF)    - Hourly Telemetry
```

---

## 🎨 Visual Aesthetics & 3D Interactive Design System

### 1. Design Philosophy
- **Dark Modern Fintech Luxury:** Deep Obsidian `#030712`, Slate `#0f172a`, and Navy `#0a1128` background with vivid glowing accents (Electric Indigo `#6366f1`, Cyber Emerald `#10b981`, Warning Amber `#f59e0b`, and Neon Crimson `#f43f5e`).
- **Glassmorphism & Radial Mesh Gradients:** `backdrop-blur-xl`, subtle 1px border glows (`border-white/10` and `border-indigo-500/20`), inner shadows, and dynamic gradient orbs following mouse movement.
- **Typography:** `Inter` for clean corporate interfaces combined with `JetBrains Mono` or `Consolas` for cryptographic hashes, transaction IDs, nonces, and currency amounts.

### 2. 3D Interactive Payment Cards (`Card3D.tsx`)
Create a **hyper-realistic 3D payment card component** for UPI debit cards, RuPay credit cards, and merchant virtual cards:
- **Physics-Based Mouse Tilt:** Use `framer-motion` with `useMotionValue`, `useTransform`, and `useSpring` to dynamically rotate the card in 3D space (`rotateX`, `rotateY`, `scale: 1.05`, perspective: `1000px`).
- **Interactive Glare & Holographic Sheen:** Dynamic specular reflection overlay that shifts highlights across the card surface as the user tilts their mouse.
- **Card Flip Animation:** Smooth 180-degree 3D flip toggling between **Front** (EMV Chip with gold sheen, contactless wave icon, embossed 16-digit card/UPI handle, bank logo, RuPay/UPI holographic badge) and **Back** (Magnetic stripe, signature panel, encrypted CVV, authorized signature text, RSA-2048 security watermark).
- **Custom Skin Variants:**
  - `Obsidian Platinum` (Executive black brushed metal with silver foil accents)
  - `Cyber UPI Mesh` (Dark iridescent neon gradient with animated mesh glow)
  - `Emerald Sovereign` (Deep forest emerald with gold embossed typography)

### 3. Sticky Scroll Storytelling (`StickyScrollReveal.tsx`)
Implement an **Aceternity UI-style Sticky Scroll Reveal** component on the landing page and architecture pages to guide users through complex flows:
- **UPI 2.0 Transaction Lifecycle (8 Steps):**
  1. *Payment Initiation & RSA Signing* (Client offline/online cryptographic signature)
  2. *Gateway Perimeter Defense* (Token-bucket rate limiting & JWT verification)
  3. *Sub-10ms Risk Scoring* (Behavioral velocity & IP geolocation evaluation)
  4. *Kafka Distributed Streaming* (`payment_initiated` event dispatch)
  5. *Atomic Dual-Ledger Mutation* (Debit sender & credit receiver in MySQL)
  6. *Python ML Real-Time Inference* (XGBoost/RF fraud probability scoring)
  7. *AI Explainability & SHAP* (Generative natural language security analysis)
  8. *NPCI & Banking Settlement* (Inter-bank ISO-8583 switch clearing)
- As the user scrolls, the active text description stays smoothly pinned while the corresponding right-side 3D interactive graphic or terminal code preview seamlessly transitions with physics-based springs.

### 4. 3D Floating Feature Cards & Bento Grids
- **Bento Grid Architecture:** Multi-column asymmetric card layouts displaying live microservice metrics, real-time TPS, fraud cluster network mini-graphs, and bank gateway health gauges.
- **Hover Micro-Animations:** Subtle border tracing beams (`border-beam` animation), glowing gradient backdrops, and card float elevation on mouse hover.

### 5. AI Generated Visual Assets & Art Direction
Utilize high-fidelity AI-generated visuals for visual impact:
- **Hero Artwork (`/public/hero-visual.jpg`):** Quantum-level financial data mesh, glowing payment packets traversing a dark futuristic city network.
- **Fraud Ring Visual (`/public/fraud-network-problem.jpg`):** Multi-hop cybercrime syndicate nodes highlighted in neon red and amber.
- **Biometric Scanner (`/public/kyc-biometric-mesh.jpg`):** Holographic 3D face mesh scan and Aadhaar QR biometric validation.
- **Offline Mesh Satellite (`/public/offline-mesh-sync.jpg`):** Peer-to-peer decentralized mesh relay for payments without cellular connectivity.

---

## 🔐 Bank-Grade Cryptography & Zero-Trust Client Implementation

The frontend must natively replicate the backend's cryptographic security layer:

### 1. Zero-Trust Token Lifecycle (`src/lib/api/client.ts`)
- **Stateless Bearer JWT:** Attached to all protected requests (`Authorization: Bearer <token>`).
- **Seamless Refresh Token Rotation:** Intercept `401 Unauthorized` responses, queue concurrent requests, invoke `POST /auth/refresh-token`, update stored tokens in `localStorage` / `sessionStorage`, and transparently replay original requests.
- **Auto-Logout & Session Purge:** Invalidate all local storage and broadcast a logout event across all open browser tabs via `BroadcastChannel` if refresh fails.

### 2. Client-Side RSA-2048 Digital Signatures (`src/lib/crypto/rsaSigner.ts`)
For online high-value transactions and offline mesh synchronization:
- Use the native browser **WebCrypto API** (`window.crypto.subtle`) to generate an RSA-PSS / RSA-SHA256 keypair (`2048-bit`).
- Store the private key securely in IndexedDB (`cryptoKeyStore`).
- Export and register the public key with the backend (`POST /payment/register-key`).
- Canonicalize transaction payload:
  $$\mathcal{M} = \text{senderUpiId} \parallel \text{receiverUpiId} \parallel \text{amount} \parallel \text{timestamp} \parallel \text{nonce}$$
- Sign $\mathcal{M}$ with private key and attach Base64 digital signature to `X-Signature` header and request body.

### 3. Client-Side AES-256-GCM Encryption (`src/lib/crypto/aesGcm.ts`)
For sensitive KYC PII (Aadhaar 12-digit UID and PAN numbers) before network transmission:
- Derive or negotiate a shared session key via WebCrypto.
- Generate a cryptographically random 12-byte (96-bit) IV using `window.crypto.getRandomValues(new Uint8Array(12))`.
- Encrypt data using `AES-GCM` with a 128-bit authentication tag.
- Transmit payload as Base64-encoded `IV + Ciphertext + Tag`.

### 4. Idempotency & Replay Defense
- Generate a unique `UUIDv4` for every transactional action.
- Inject header: `Idempotency-Key: <uuid>`.
- Display visual feedback if a duplicate submission is detected within the 24-hour window.

### 5. Rate-Limiting Graceful Degradation
- Catch HTTP `429 Too Many Requests` from the Spring Cloud Gateway Redis token bucket.
- Parse `Retry-After` header.
- Trigger a dynamic visual countdown timer and disable action buttons with clear user communication.

---

## 💻 Granular Component & Page Implementation Blueprint

### 1. Landing Page (`/page.tsx`)
- **3D Hero Section:** 
  - Dynamic headline: *"See risk before it becomes loss."*
  - Floating 3D interactive RuPay/UPI holographic card on the right that tilts with mouse movement.
  - Live metric ticker: Median Risk Latency (`< 8.4 ms`), Model Precision (`99.98%`), Active Microservices (`27 Services`), Audit Ledger Coverage (`100%`).
  - Interactive *"Try Live Payment Demo"* button that opens an in-page interactive modal.
- **Interactive Sticky Scroll Section:**
  - Sticky scroll reveal of the 8-stage transaction lifecycle.
- **Multi-Hop Fraud Cluster Interactive Demo:**
  - Visual canvas depicting a real-time money laundering ring (Sender -> Mule Hub -> Beneficiary) with live risk score calculation (940/1000 - Critical Risk).
- **Compliance & Security Marquee:**
  - Bank badges (RBI Master Directions, PMLA 2002, DPDP Act 2023, PCI-DSS Level 1, NPCI UPI 2.0).

---

### 2. Operations & Intelligence Console (`/dashboard/page.tsx`)
- **System Health Mesh:** Live latency status for all 27 microservices with real-time ping indicator (Green = `< 15ms`, Amber = `15-50ms`, Red = `Unavailable`).
- **Live Transaction Stream:** Table/Feed of incoming transactions auto-updating with animated entry transitions, colored risk badges (`SAFE`, `SUSPICIOUS`, `CRITICAL`), and 1-click investigation trigger.
- **Key Metric StatCards:** Gross Merchandise Value (GMV), 24h Transaction Volume, Real-Time Fraud Intercepts, AutoPay Active Mandates.
- **Global Command Palette (`CommandPalette.tsx`):** Pressing `Ctrl+K` or `Cmd+K` launches fuzzy search for transactions, UPI VPAs, merchant IDs, AML alerts, and quick navigation.

---

### 3. Payment Execution & Verification Hub (`/payments/page.tsx`)
- **Interactive Payment Form:**
  - Dynamic VPA Resolution: Auto-resolves IFSC, Bank Name, and Payee Name as the user types `@okaxis`, `@okhdfcbank`, `@ybl`.
  - Amount input with animated currency formatting (`₹`).
  - 3D Interactive Card Preview that updates with sender details in real-time.
- **Security Verification Step:**
  - 6-Digit MPIN keypad with sound/haptic feedback simulation and masked PIN dots.
  - Instant RSA payload signature generation indicator.
- **Payment Result Modal / 3D Receipt:**
  - Dynamic animated checkmark with `canvas-confetti` burst on success.
  - Real-time Risk Score badge (`Score: 12/1000 - Approved`).
  - Downloadable branded PDF Tax Invoice (connected to `/invoice/{id}/pdf`).
  - Dynamic ZXing QR code for receipt verification.

---

### 4. Offline Mesh Payment & Sync Terminal (`/payments/offline/page.tsx`)
- **Simulated Airplane Mode:** Toggle button simulating total network disconnection.
- **Offline Ledger in IndexedDB:** Allows user to initiate offline transactions using local RSA-2048 keypair.
- **Mesh Sync Queue:** Visual queue displaying pending unsynchronized transactions with cryptographically generated nonces.
- **Sync Now Action:** Calls `POST /sync/process` to push queued signed transactions to `SyncService (:8089)` upon re-establishing connection.

---

### 5. Fraud Detection & War Room (`/fraud/page.tsx` & `/war-room/page.tsx`)
- **Live Telemetry Stream:** Real-time stream of flagged transactions from `FraudService (:8086)`.
- **Explainable AI (XAI) Modal (`XaiShapModal.tsx`):**
  - Waterfall chart displaying SHAP (Shapley Additive exPlanations) values for the active ML prediction.
  - Generative explanation generated via Spring AI (`/ai/fraud-explain`).
- **Attack Vector Simulator (`/simulations`):**
  - Run synthetic fraud attacks: *Smurfing / Structuring*, *Velocity Spikes*, *Account Takeover*, *Simulated Night-time Transacting*.
  - View instant decision tree output from the Risk Rule Engine.

---

### 6. KYC & Regulatory Compliance Center (`/kyc/page.tsx`)
- **3-Tier KYC Progression Stepper:**
  - *Tier 0 (Unverified):* Limited to ₹10,000/mo.
  - *Tier 1 (Aadhaar OTP):* Live OTP input with countdown timer. Encrypted with AES-256-GCM. Unlocks ₹50,000/mo.
  - *Tier 2 (Full KYC):* Real-time PAN validation with NSDL simulator + WebCam liveness capture with 3D face mesh scan animation. Unlocks unlimited limits.
- **Immutable Audit Trail:** Chronological ledger of KYC status transitions signed with timestamps.

---

### 7. Banking Rails, NPCI & Reconciliation (`/reconciliation/page.tsx` & `/settlement/page.tsx`)
- **3-Way Reconciliation Audit View:**
  - Visual comparison table: Internal Ledger (`TransactionService`) vs NPCI Switch (`NPCI`) vs Core Banking Statement (`BankGateway`).
  - Mismatch discrepancy highlighting (e.g. ₹5,000 debited in Core Bank but marked PENDING in NPCI).
  - 1-Click Resolve Action (`PUT /reconciliation/discrepancy/{id}/resolve`).
  - Download Audit-Ready Excel Sheet (`GET /reconciliation/report/{id}/export`).
- **End-of-Day Settlement Dashboard:**
  - Displays daily settlement batches with 0.2% commission and 18% GST calculations.
  - Manual Trigger Batch button (`POST /settlement/run`).

---

### 8. AI Investigator Copilot & MCP Tools (`/ai-investigator/page.tsx` & `/mcp/page.tsx`)
- **Conversational Chat Interface:**
  - Connects to Spring AI `/ai/chat` with chat memory.
  - Quick action prompt chips: *"Analyze spending anomalies for user vikram@okaxis"*, *"Explain why payment #PAY-9921 was blocked"*, *"Check OFAC sanctions for entity GlobalTraders LLC"*.
- **Model Context Protocol (MCP) Explorer:**
  - Real-time listing of registered MCP tools (`fetch_transaction_graph`, `query_aml_sanctions`, `trigger_wallet_freeze`).
  - Interactive tool execution console with JSON request/response inspector.

---

## 🛠️ Step-by-Step Implementation Sequence

Execute the implementation in strict priority order:

1. **Phase 1: Design Tokens & Base Primitives**
   - Configure `tailwind.config.ts` with customized dark mode palette, fintech color tokens, and custom animation keyframes (`tilt`, `shine`, `pulse-glow`, `accordion`).
   - Enhance `src/styles/globals.css` with CSS variables for glassmorphism, 3D transform utilities (`transform-style: preserve-3d`), and scrollbar styling.
   - Expand `shadcn/ui` primitives (`Button`, `Card`, `Badge`, `Modal`, `DataTable`, `Tabs`, `Input`, `Tooltip`).

2. **Phase 2: 3D Interactive Animation Components**
   - Build `Card3D.tsx` with Framer Motion 3D tilt, holographic sheen, and card flip.
   - Build `StickyScrollReveal.tsx` for the 8-stage transaction lifecycle.
   - Build `ConfettiSuccess.tsx` for satisfying transaction completions.

3. **Phase 3: Cryptography & Security Layer**
   - Implement `src/lib/crypto/rsaSigner.ts` (WebCrypto RSA-2048 keypair generation & payload signing).
   - Implement `src/lib/crypto/aesGcm.ts` (WebCrypto AES-256-GCM encryption for KYC PII).
   - Refactor `src/lib/api/client.ts` to support auto-refresh token rotation, idempotency header injection, and rate-limit retry backoff.

4. **Phase 4: Microservice API Client Expansion**
   - Implement typed API client wrappers in `src/lib/api/` for all 27 microservices:
     - `payment-client.ts`, `wallet-client.ts`, `kyc-client.ts`, `aml-client.ts`, `risk-client.ts`, `fraud-client.ts`, `reconciliation-client.ts`, `settlement-client.ts`, `ai-client.ts`, `ml-client.ts`, `invoice-client.ts`, `dispute-client.ts`.

5. **Phase 5: Page Upgrades & Feature Delivery**
   - Transform `src/app/page.tsx` into a high-converting 3D fintech landing page.
   - Implement interactive Payment Execution & 3D Receipt on `/payments`.
   - Implement KYC 3-Tier Verification with simulated webcam scanner on `/kyc`.
   - Implement 3-Way Reconciliation & Settlement ledger on `/reconciliation` and `/settlement`.
   - Implement Fraud War Room with SHAP attribution and attack simulation on `/war-room` and `/simulations`.

6. **Phase 6: Visual Polish, Error Boundaries & Testing**
   - Ensure all API calls have graceful offline/fallback states using the Honest Unavailable State pattern.
   - Verify keyboard accessibility (`Cmd+K` palette, tab navigation).
   - Test build correctness (`npm run build`) and responsiveness across mobile, tablet, and ultra-wide screens.

---

## ⚡ Non-Negotiable Success Criteria
1. **Visual "WOW" Factor:** The application must feel as polished and fluid as Stripe or Apple Pay, featuring 3D cards, smooth spring micro-interactions, and premium dark glass aesthetics.
2. **Zero Cryptographic Flaws:** RSA signatures and AES-256-GCM encryption must adhere strictly to the cryptographic specifications laid out in `docs/SECURITY_CRYPTOGRAPHY_COMPLIANCE.md`.
3. **End-to-End Resilience:** Every microservice route must fail gracefully with descriptive error alerts, retry mechanisms, and contract inspection modals.
4. **Zero Placeholder Text:** All metrics, transaction logs, and data visualizations must reflect realistic Indian banking data (UPI VPAs, RuPay cards, INR amounts, IFSC codes, UIDAI Aadhaar formats).
