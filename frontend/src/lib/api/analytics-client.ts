import { apiClient } from './client';

export const analyticsClient = {
  getSummaryMetrics: async (): Promise<Record<string, number>> => {
    return apiClient.get<Record<string, number>>('/api/v1/analytics/summary');
  },
};
