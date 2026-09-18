export interface PaymentRequest {
  senderUpiId: string;
  receiverUpiId: string;
  amount: number;
  remarks?: string;
  signature?: string;
  nonce?: string;
}

export interface PaymentResponse {
  paymentId: string;
  status: 'INITIATED' | 'COMPLETED' | 'FAILED' | 'FLAGGED';
  riskScore: number;
  timestamp: string;
}
