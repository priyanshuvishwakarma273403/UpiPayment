# SentinelX — Authentication Architecture

## Overview
Authentication in SentinelX is integrated with the Spring Boot `AuthService` microservice (`com.authService`).

## Protocol & Flow
1. **Login:** User submits credentials via `/login` to `POST /auth/login`.
2. **JWT Response:** Backend issues signed JWT token and Refresh Token containing claims for `userId`, `role`, and `upiId`.
3. **Storage:** Standardized session context managed via `AuthProvider.tsx` stored securely in HTTP-only state/cookies.
4. **Token Refresh:** Automatic rotation via `POST /auth/refresh-token` before token expiry.
5. **Gateway Authorization:** Gateway verifies `Authorization: Bearer <jwt_token>` header on all microservice requests.
