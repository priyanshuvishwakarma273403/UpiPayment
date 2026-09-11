import { ContractStatus } from './api';

export interface Merchant {
  merchantId: string;
  businessName: string;
  category: string;
  riskScore: number;
  status: 'ACTIVE' | 'HIGH_RISK' | 'BLOCKED';
  totalTransactionsCount: number;
  _contractStatus?: ContractStatus;
}
