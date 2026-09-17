import { apiClient } from './client';
import { Merchant } from '@/types/merchant';

export const merchantClient = {
  getMerchants: async (): Promise<Merchant[]> => {
    return apiClient.get<Merchant[]>('/api/v1/merchants');
  },
  getMerchantById: async (id: string): Promise<Merchant> => {
    return apiClient.get<Merchant>(`/api/v1/merchants/${id}`);
  },
};
