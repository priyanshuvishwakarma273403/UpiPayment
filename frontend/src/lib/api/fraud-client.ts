import { apiClient } from './client';
import { FraudAlert } from '@/types/fraud';

export const fraudClient = {
  getAlerts: async (): Promise<FraudAlert[]> => {
    return apiClient.get<FraudAlert[]>('/fraud/alerts');
  },
  getAlertById: async (id: string): Promise<FraudAlert> => {
    return apiClient.get<FraudAlert>(`/fraud/alerts/${id}`);
  },
};
