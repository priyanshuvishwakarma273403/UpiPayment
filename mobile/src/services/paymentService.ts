import { apiClient } from '../lib/apiClient';
import { API_CONFIG } from '../constants/api';

export const paymentService = {
  pay: async (payload: { senderUpiId: string; receiverUpiId: string; amount: number }) => {
    // Boilerplate stub
  },
  offlinePay: async (payload: any) => {
    // Boilerplate stub
  },
  verifyChecksum: async (paymentId: string) => {
    // Boilerplate stub
  },
};
