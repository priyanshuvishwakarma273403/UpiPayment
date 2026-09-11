import { ContractStatus } from './api';

export interface InvestigationReport {
  investigationId: string;
  targetCustomerId: string;
  evidence: Record<string, unknown>;
  timeline: Array<Record<string, unknown>>;
  riskSignals: Array<Record<string, unknown>>;
  potentialFraudType: string;
  confidenceScore: number;
  missingEvidence: string[];
  recommendedAction: string;
  requiresHumanConfirmation: boolean;
  completedAt: string;
  _contractStatus?: ContractStatus;
}
