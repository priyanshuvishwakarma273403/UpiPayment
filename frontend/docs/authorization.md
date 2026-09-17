# SentinelX — Authorization & RBAC System

## Overview
SentinelX implements Role-Based Access Control (RBAC) mirrored between frontend UI guards (`src/lib/authz.ts`) and backend Spring Security microservice controllers.

## Roles
- `ADMIN`: Full platform access, user clearance management, system reset.
- `RISK_ANALYST`: Risk scoring rules, velocity vector thresholds, model retraining trigger.
- `FRAUD_ANALYST`: Investigation cases, SHAP explanation breakdown, model approval.
- `INVESTIGATOR`: Case queue management, copilot diagnostic sessions, status updating.
- `AUDITOR`: Immutable audit log inspection, OpenTelemetry trace review.
- `OPERATIONS`: Payment stream monitoring, merchant settlement ledger inspection.

## Helper Functions (`src/lib/authz.ts`)
- `canViewTransaction(user)`
- `canInvestigateFraud(user)`
- `canModifyRiskRules(user)`
- `canRetrainModels(user)`
- `canExecuteMcpTools(user)`
- `canViewAudit(user)`
- `canManageUsers(user)`
- `canApproveAction(user)`
