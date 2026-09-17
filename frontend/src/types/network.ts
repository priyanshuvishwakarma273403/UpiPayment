import { ContractStatus } from './api';

export interface NetworkNode {
  id: string;
  label: string;
  type: 'Customer' | 'Account' | 'Device' | 'IP' | 'Merchant' | 'Beneficiary' | 'Transaction';
  riskScore?: number;
}

export interface NetworkEdge {
  id: string;
  source: string;
  target: string;
  relation: 'USES_DEVICE' | 'CONNECTS_FROM' | 'OWNS_ACCOUNT' | 'SENDS_TO' | 'PERFORMED' | 'PURCHASED_FROM' | 'LINKED_TO';
}

export interface FraudNetworkGraph {
  nodes: NetworkNode[];
  edges: NetworkEdge[];
  _contractStatus?: ContractStatus;
}
