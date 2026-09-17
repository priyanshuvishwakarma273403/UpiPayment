/**
 * SentinelX Enterprise Authorization System (RBAC)
 * 
 * Provides permission check helpers matching the Spring Boot microservice security specs.
 * Roles: ADMIN, RISK_ANALYST, FRAUD_ANALYST, INVESTIGATOR, AUDITOR, OPERATIONS.
 */

export type UserRole = 
  | 'ADMIN'
  | 'RISK_ANALYST'
  | 'FRAUD_ANALYST'
  | 'INVESTIGATOR'
  | 'AUDITOR'
  | 'OPERATIONS'
  | 'ROLE_ADMIN'
  | 'ROLE_RISK_ANALYST'
  | 'ROLE_FRAUD_ANALYST'
  | 'ROLE_INVESTIGATOR'
  | 'ROLE_AUDITOR'
  | 'ROLE_OPERATIONS';

export interface UserContext {
  id?: string;
  role?: string;
  roles?: string[];
  email?: string;
}

/**
 * Normalizes role strings to standard canonical form (without ROLE_ prefix).
 */
export function normalizeRole(role: string): string {
  return role.replace(/^ROLE_/, '').toUpperCase();
}

/**
 * Extracts normalized roles array from UserContext.
 */
export function getUserRoles(user?: UserContext | null): string[] {
  if (!user) return [];
  if (user.roles && user.roles.length > 0) {
    return user.roles.map(normalizeRole);
  }
  if (user.role) {
    return [normalizeRole(user.role)];
  }
  return [];
}

/**
 * Checks if user has at least one of the target required roles.
 */
export function hasAnyRole(user: UserContext | null | undefined, targetRoles: string[]): boolean {
  const userRoles = getUserRoles(user);
  if (userRoles.includes('ADMIN')) return true; // ADMIN bypasses all permission checks
  return targetRoles.some((tr) => userRoles.includes(normalizeRole(tr)));
}

// ============================================================================
// DOMAIN PERMISSION HELPERS
// ============================================================================

/** Can view transactions, payment details, and spend summaries */
export function canViewTransaction(user?: UserContext | null): boolean {
  return hasAnyRole(user, ['ADMIN', 'RISK_ANALYST', 'FRAUD_ANALYST', 'INVESTIGATOR', 'AUDITOR', 'OPERATIONS']);
}

/** Can view and manage fraud cases, execute copilot diagnostics, and assign cases */
export function canInvestigateFraud(user?: UserContext | null): boolean {
  return hasAnyRole(user, ['ADMIN', 'RISK_ANALYST', 'FRAUD_ANALYST', 'INVESTIGATOR']);
}

/** Can modify risk scoring rules, velocity vectors, and thresholds */
export function canModifyRiskRules(user?: UserContext | null): boolean {
  return hasAnyRole(user, ['ADMIN', 'RISK_ANALYST']);
}

/** Can trigger continuous ML training, approve candidate models for production */
export function canRetrainModels(user?: UserContext | null): boolean {
  return hasAnyRole(user, ['ADMIN', 'RISK_ANALYST', 'FRAUD_ANALYST']);
}

/** Can execute Model Context Protocol (MCP) diagnostic tools */
export function canExecuteMcpTools(user?: UserContext | null): boolean {
  return hasAnyRole(user, ['ADMIN', 'FRAUD_ANALYST', 'INVESTIGATOR']);
}

/** Can view security audit logs and OpenTelemetry trace logs */
export function canViewAudit(user?: UserContext | null): boolean {
  return hasAnyRole(user, ['ADMIN', 'AUDITOR', 'RISK_ANALYST']);
}

/** Can perform high-impact admin actions (user management, API keys, system reset) */
export function canManageUsers(user?: UserContext | null): boolean {
  return hasAnyRole(user, ['ADMIN']);
}

/** Can approve sensitive financial transactions, freeze/unfreeze wallets, or file SARs */
export function canApproveAction(user?: UserContext | null): boolean {
  return hasAnyRole(user, ['ADMIN', 'RISK_ANALYST', 'FRAUD_ANALYST']);
}
