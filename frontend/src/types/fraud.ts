import { ContractStatus } from './api';

export interface FraudAlert {
  alertId: string;
  transactionId: string;
  customerId: string;
  fraudType: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'OPEN' | 'INVESTIGATING' | 'ESCALATED' | 'CONFIRMED_FRAUD' | 'FALSE_POSITIVE' | 'RESOLVED';
  detectedAt: string;
  _contractStatus?: ContractStatus;
}
