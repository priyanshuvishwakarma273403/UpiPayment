import { ContractStatus } from './api';

export interface Notification {
  id: string;
  title: string;
  message: string;
  severity: 'INFO' | 'WARNING' | 'ALERT' | 'CRITICAL';
  read: boolean;
  createdAt: string;
  _contractStatus?: ContractStatus;
}
