import { apiClient } from './client';
import { Transaction } from '@/types/transaction';

export const transactionClient = {
  getTransactions: async (params?: Record<string, string | number | boolean>): Promise<Transaction[]> => {
    return apiClient.get<Transaction[]>('/api/v1/transactions', { params });
  },
  getTransactionById: async (id: string): Promise<Transaction> => {
    return apiClient.get<Transaction>(`/api/v1/transactions/${id}`);
  },
};
