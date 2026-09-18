export interface Transaction {
  id: string;
  rrn: string;
  amount: number;
  senderUpiId: string;
  receiverUpiId: string;
  status: 'SUCCESS' | 'PENDING' | 'FAILED';
  createdAt: string;
  type: 'DEBIT' | 'CREDIT';
}
