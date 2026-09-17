import { apiClient } from './client';
import { FraudNetworkGraph } from '@/types/network';

export const networkClient = {
  getFraudNetwork: async (customerId: string): Promise<FraudNetworkGraph> => {
    return apiClient.get<FraudNetworkGraph>(`/fraud/network/${customerId}`);
  },
};
