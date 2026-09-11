import { ContractStatus } from './api';

export interface Customer {
  customerId: string;
  name: string;
  email: string;
  phone: string;
  upiId: string;
  accountAgeDays: number;
  riskScore: number;
  status: 'ACTIVE' | 'SUSPENDED' | 'FROZEN';
  createdAt: string;
  _contractStatus?: ContractStatus;
}
