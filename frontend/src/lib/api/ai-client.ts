import { apiClient } from './client';
import { InvestigationReport } from '@/types/investigation';

export const aiClient = {
  investigateCustomer: async (customerId: string): Promise<InvestigationReport> => {
    return apiClient.post<InvestigationReport>('/fraud/agent/investigate', { customerId });
  },
};
