# SentinelX Frontend Architecture & Engineering Guide

## 1. Overview & Product Purpose

**SentinelX** is an enterprise-grade financial fraud detection, real-time risk scoring, and intelligence platform. The frontend application is built from scratch using Next.js 14+ (App Router), TypeScript, Tailwind CSS, TanStack Query, Zod, and React Hook Form.

### Design Principles:
1. **Light Theme First:** Built with clean white/off-white background surfaces (`#ffffff`, `#f8fafc`), slate typography (`#0f172a`), and crisp subtle borders (`#e2e8f0`). High information density without visual clutter or superficial aesthetic distractions.
2. **Anti-Hallucination Policy:** Strict contract boundary enforcement. All unverified API contracts are explicitly typed and marked with `BACKEND_CONTRACT_PENDING`. No fake business data or false mock API responses are ever generated.
3. **Modular Feature-Driven Architecture:** Clean separation between App Router route handlers (`src/app`), shared UI primitives (`src/components/ui`), app shell layouts (`src/components/layout`), centralized API clients (`src/lib/api`), domain type definitions (`src/types`), global providers (`src/providers`), and client state stores (`src/stores`).

---

## 2. Directory Structure

```
d:\UpiMesh\frontend\
├── docs/
│   └── architecture.md               # Architectural & engineering specification
├── src/
│   ├── app/                          # Next.js App Router routes (28 route shells)
│   │   ├── (dashboard)/              # AppShell wrapped operational layout group
│   │   │   ├── admin/
│   │   │   ├── ai-investigator/
│   │   │   ├── analytics/
│   │   │   ├── audit/
│   │   │   ├── cases/
│   │   │   │   └── [id]/
│   │   │   ├── customers/
│   │   │   │   └── [id]/
│   │   │   ├── dashboard/
│   │   │   ├── fraud/
│   │   │   │   └── [id]/
│   │   │   ├── investigations/
│   │   │   │   └── [id]/
│   │   │   ├── knowledge/
│   │   │   ├── mcp/
│   │   │   ├── merchants/
│   │   │   │   └── [id]/
│   │   │   ├── models/
│   │   │   ├── network/
│   │   │   ├── notifications/
│   │   │   ├── reports/
│   │   │   ├── risk/
│   │   │   ├── settings/
│   │   │   ├── simulations/
│   │   │   ├── transactions/
│   │   │   │   └── [id]/
│   │   │   └── layout.tsx
│   │   ├── login/
│   │   ├── onboarding/
│   │   ├── layout.tsx                # Root HTML layout with providers
│   │   └── page.tsx                  # Root redirect (-> /dashboard)
│   ├── components/
│   │   ├── layout/                   # Enterprise AppShell components (Sidebar, Topbar, Header)
│   │   └── ui/                       # Reusable UI & Loading Primitives (Button, Table, Card, Modal, etc.)
│   ├── config/
│   │   └── navigation.config.ts      # Navigation section & route configuration map
│   ├── lib/
│   │   ├── api/                      # Base client & microservice API wrappers
│   │   ├── errors.ts                 # ApiError & normalized handling
│   │   └── utils.ts                  # ClassName joiners & formatters
│   ├── providers/
│   │   ├── AuthProvider.tsx          # Client Auth context & route guard
│   │   └── QueryProvider.tsx         # TanStack Query Client provider
│   ├── stores/
│   │   ├── authStore.ts              # Zustand store for user session & tokens
│   │   └── uiStore.ts                # Zustand store for UI toggles & notifications
│   ├── styles/
│   │   └── globals.css               # Design tokens, accessibility focus ring, Tailwind directives
│   └── types/                        # Enterprise domain TypeScript definitions
│       ├── api.ts
│       ├── audit.ts
│       ├── case.ts
│       ├── customer.ts
│       ├── fraud.ts
│       ├── investigation.ts
│       ├── merchant.ts
│       ├── model.ts
│       ├── network.ts
│       ├── notification.ts
│       ├── risk.ts
│       └── transaction.ts
├── .env.example                      # Environment template
├── .env.local                        # Local development environment overrides
├── next.config.mjs                   # Next.js build configuration
├── postcss.config.js                 # PostCSS configuration for Tailwind
├── tailwind.config.ts                # Tailwind design token configuration
└── tsconfig.json                     # Strict TypeScript config with @/* path alias
```

---

## 3. Centralized API Architecture & Guardrails

All API requests pass through the unified client infrastructure in `src/lib/api/client.ts`.

### Key Features:
- **Base Fetch Interceptor:** Handles JWT token insertion (`Authorization: Bearer <token>`), response JSON parsing, and unified error throw behavior (`ApiError`).
- **Domain API Clients:**
  - `auth-client.ts`: User authentication, session retrieval, logout.
  - `transaction-client.ts`: Payment stream search & detail lookup.
  - `risk-client.ts`: Risk score evaluation, feature importance, velocity metrics.
  - `fraud-client.ts`: Fraud alert queue management, triage tagging.
  - `case-client.ts`: Case file lifecycle, assignment, notes.
  - `customer-client.ts`: Customer profile, risk tiering, device linkages.
  - `merchant-client.ts`: Merchant risk profiling, chargeback rates.
  - `analytics-client.ts`: Aggregated loss metrics & trend analytics.
  - `ai-client.ts`: Fraud Investigation Copilot execution & RAG queries.
  - `network-client.ts`: Graph node/edge queries for entity networks.
  - `notification-client.ts`: Real-time system notifications & webhooks.
- **Contract Pending Guardrail:** Every API response type includes `_contractStatus: 'BACKEND_CONTRACT_PENDING' | 'VERIFIED'`.

---

## 4. State Management Strategy

1. **Server State & Data Fetching:** **TanStack Query (`@tanstack/react-query`)** manages caching, background polling, stale-time invalidation, and optimistic mutations.
2. **Client UI & Auth Session State:** **Zustand (`authStore.ts`, `uiStore.ts`)** manages sidebar expansion, global modal states, active toast notifications, and user authentication tokens.

---

## 5. UI Component Primitives & Accessibility

- **Button (`src/components/ui/Button.tsx`):** Supports `primary`, `secondary`, `outline`, `danger`, `ghost` variants with loading indicators and accessible focus rings.
- **Input (`src/components/ui/Input.tsx`):** Standardized input with error message state, helper text, and left/right icon slots.
- **Card (`src/components/ui/Card.tsx`):** Enterprise container with header, title, description, body, and footer slots.
- **Table (`src/components/ui/Table.tsx`):** High-density tabular data viewer supporting loading skeletons and empty states.
- **Badge (`src/components/ui/Badge.tsx`):** Status indicator pills for risk levels (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `INFO`).
- **Modal (`src/components/ui/Modal.tsx`):** Accessible dialog backdrop with keyboard escape handler and focus trap.
- **SkeletonLoader & ErrorState (`src/components/ui/`):** Standardized loading & error placeholders across all dashboard views.

---

## 6. Route Navigation Map (28 Routes)

| Domain | Path | Description |
|---|---|---|
| Auth | `/login` | Analyst authentication portal |
| Auth | `/onboarding` | System initialization & clearance verification |
| Operations | `/dashboard` | System overview, throughput, live alerts |
| Operations | `/transactions` | Real-time UPI transaction stream |
| Operations | `/transactions/[id]` | Deep transaction feature & risk detail |
| Operations | `/risk` | Live risk vector monitoring & thresholds |
| Operations | `/fraud` | Fraud alert triage queue |
| Operations | `/fraud/[id]` | Fraud alert detail & rule breakdown |
| Operations | `/cases` | Case management workspace |
| Operations | `/cases/[id]` | Detailed case file & timeline |
| Operations | `/investigations` | Active fraud investigation dossiers |
| Operations | `/investigations/[id]` | Multi-entity investigation dossier |
| Graph | `/customers` | Customer risk directory |
| Graph | `/customers/[id]` | Customer profile & device linkages |
| Graph | `/merchants` | Merchant risk directory |
| Graph | `/merchants/[id]` | Merchant profile & chargeback trends |
| Graph | `/network` | Fraud network graph visualization |
| AI | `/ai-investigator` | AI Fraud Investigation Copilot |
| AI | `/models` | Continuous model learning & registry |
| AI | `/knowledge` | Policy RAG vector knowledge base |
| AI | `/mcp` | Model Context Protocol tool registry |
| AI | `/simulations` | Fraud attack simulator sandbox |
| Governance | `/analytics` | Historical metrics & loss prevention |
| Governance | `/reports` | Regulatory SAR filings & exports |
| Governance | `/notifications` | Dispatch logs & broadcast alerts |
| Governance | `/audit` | Immutable security audit log |
| Governance | `/settings` | Investigator workspace settings |
| Governance | `/admin` | System admin & microservice health |

---

## 7. Verification & Build Commands

Inside `d:\UpiMesh\frontend`:

```bash
# Install dependencies
npm install

# Run TypeScript type check
npx tsc --noEmit

# Execute Next.js production build
npm run build

# Start production server
npm run start
```
