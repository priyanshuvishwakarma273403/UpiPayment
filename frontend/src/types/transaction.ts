import { ContractStatus } from './api';

/**
 * Transaction Domain Contract
 * STATUS: BACKEND_CONTRACT_PENDING (Subject to backend API contract verification)
 */
export interface Transaction {
  id: string;
  paymentId: string;
  senderId: string;
  senderUpiId: string;
  receiverId: string;
  receiverUpiId: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'BLOCKED' | 'SUSPICIOUS';
  paymentMode: 'UPI_INTENT' | 'UPI_COLLECT' | 'QR';
  deviceId: string;
  ipAddress: string;
  timestamp: string;
  _contractStatus?: ContractStatus;
}
