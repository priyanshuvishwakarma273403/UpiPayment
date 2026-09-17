import { apiClient } from './client';
import { Customer } from '@/types/customer';

export const customerClient = {
  getCustomers: async (): Promise<Customer[]> => {
    return apiClient.get<Customer[]>('/api/v1/customers');
  },
  getCustomerById: async (id: string): Promise<Customer> => {
    return apiClient.get<Customer>(`/api/v1/customers/${id}`);
  },
};
