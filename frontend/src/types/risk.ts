import { ContractStatus } from './api';

export type RiskLevel = 'LOW_RISK' | 'SUSPICIOUS' | 'HIGH_RISK' | 'REQUIRES_INVESTIGATION';

export interface RiskSignal {
  signalId: string;
  signalName: string;
  category: string;
  triggered: boolean;
  scoreContribution: number;
  explanation: string;
}

export interface RiskAssessment {
  paymentId: string;
  riskScore: number;
  riskLevel: RiskLevel;
  decision: 'ALLOWED' | 'REVIEW' | 'BLOCKED';
  signals: RiskSignal[];
  evaluatedAt: string;
  processingTimeMs: number;
  _contractStatus?: ContractStatus;
}
