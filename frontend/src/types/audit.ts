import { ContractStatus } from './api';

export interface AuditEvent {
  eventId: string;
  timestamp: string;
  category: string;
  action: string;
  actor: string;
  status: 'SUCCESS' | 'DENIED' | 'FAILED';
  details: Record<string, unknown>;
  _contractStatus?: ContractStatus;
}
