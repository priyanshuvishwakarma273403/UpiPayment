import { apiClient } from './client';
import { Case } from '@/types/case';

export const caseClient = {
  getCases: async (): Promise<Case[]> => {
    return apiClient.get<Case[]>('/fraud/cases');
  },
  getCaseById: async (id: string): Promise<Case> => {
    return apiClient.get<Case>(`/fraud/cases/${id}`);
  },
  createCase: async (payload: Partial<Case>): Promise<Case> => {
    return apiClient.post<Case>('/fraud/cases', payload);
  },
  updateCaseStatus: async (id: string, status: string, reason?: string): Promise<Case> => {
    return apiClient.put<Case>(`/fraud/cases/${id}/status`, { status, reason });
  },
};
