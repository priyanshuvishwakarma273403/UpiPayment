# 📱 UPI Mesh — Mobile Architecture & Setup

## Tech Stack
- **Framework:** Expo SDK 51+ (Managed Workflow with Prebuild support)
- **Routing:** Expo Router v3 (File-based routing, identical to Next.js App Router)
- **Styling:** NativeWind v4 (Tailwind CSS for React Native, sharing color tokens with Web)
- **Animations:** React Native Reanimated v3 + React Native Gesture Handler
- **Icons:** Lucide React Native
- **State Management:** Zustand
- **Networking:** TanStack React Query v5 & Axios
- **Hardware & Security:**
  - `expo-local-authentication`: Biometric FaceID / Fingerprint authorization
  - `expo-secure-store`: Hardware Keystore for tokens and RSA-2048 private keys
  - `expo-camera` & `expo-barcode-scanner`: BharatQR & UPI QR scanning
  - `expo-haptics`: Physics-based tactile vibration feedback

---

## Folder Structure

```
mobile/
├── app/                        # Expo Router (File-based routing)
│   ├── _layout.tsx             # Root layout with SafeAreaProvider & Status bar
│   ├── (auth)/                 # Authentication Flow
│   │   ├── _layout.tsx
│   │   ├── login.tsx           # Sign in
│   │   ├── register.tsx        # Sign up
│   │   ├── otp.tsx             # 6-digit OTP verification
│   │   └── biometrics-setup.tsx# FaceID / TouchID onboarding
│   ├── (tabs)/                 # Main Bottom Navigation Tabs
│   │   ├── _layout.tsx         # Bottom tab bar with Lucide icons
│   │   ├── index.tsx           # Home / Digital Wallet balance
│   │   ├── scan.tsx            # Camera QR scanner
│   │   ├── pay.tsx             # Direct VPA / Phone money transfer
│   │   ├── history.tsx         # Immutable transaction ledger
│   │   └── profile.tsx         # User profile, KYC limits & security
│   ├── payment/
│   │   ├── [id].tsx            # Payment status & receipt
│   │   ├── confirm.tsx         # 6-Digit MPIN modal & risk check
│   │   └── offline.tsx         # Offline mesh payment queue
│   ├── kyc/
│   │   ├── index.tsx           # 3-Tier KYC stepper
│   │   └── face-match.tsx      # Liveness camera capture
│   └── cards/
│       └── index.tsx           # 3D Virtual RuPay / UPI Card management
│
├── src/
│   ├── components/             # Reusable UI & Domain Components
│   │   ├── ui/                 # Button, Card, Input, Badge
│   │   ├── cards/              # Card3D (Virtual Card with Gyro/Touch tilt)
│   │   ├── payment/            # Keypad, PinInput, QrScannerOverlay
│   │   └── layout/             # Header, ScreenWrapper
│   ├── constants/              # API endpoints, Colors, Theme
│   ├── hooks/                  # useAuth, useBiometrics, usePayment, useHaptics
│   ├── lib/                    # apiClient, secureStorage, crypto helpers
│   ├── services/               # authService, paymentService, walletService, kycService, syncService
│   ├── stores/                 # authStore, walletStore, offlineSyncStore (Zustand)
│   ├── types/                  # payment.ts, transaction.ts, user.ts
│   └── utils/                  # formatting, validation
│
├── app.json                    # Expo configuration
├── babel.config.js             # Babel config with NativeWind & Reanimated plugins
├── global.css                  # Tailwind styles entry
├── metro.config.js             # Metro bundler config with NativeWind
├── nativewind-env.d.ts         # TypeScript definitions for NativeWind
├── package.json                # Project dependencies
└── tsconfig.json               # TypeScript path aliases
```
