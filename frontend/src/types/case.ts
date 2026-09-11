import { ContractStatus } from './api';

export interface CaseAuditEntry {
  actor: string;
  action: string;
  timestamp: string;
  entity: string;
  oldState?: string;
  newState?: string;
  notes?: string;
}

export interface Case {
  caseId: string;
  customerId: string;
  transactionId: string;
  fraudType: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'OPEN' | 'INVESTIGATING' | 'ESCALATED' | 'CONFIRMED_FRAUD' | 'FALSE_POSITIVE' | 'RESOLVED';
  assignedTo?: string;
  createdAt: string;
  updatedAt: string;
  resolution?: string;
  resolutionReason?: string;
  auditTrail: CaseAuditEntry[];
  _contractStatus?: ContractStatus;
}
