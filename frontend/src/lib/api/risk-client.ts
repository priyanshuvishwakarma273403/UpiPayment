import { apiClient } from './client';
import { RiskAssessment } from '@/types/risk';

export const riskClient = {
  getRiskAssessment: async (paymentId: string): Promise<RiskAssessment> => {
    return apiClient.get<RiskAssessment>(`/fraud/risk/evaluate/${paymentId}`);
  },
};
