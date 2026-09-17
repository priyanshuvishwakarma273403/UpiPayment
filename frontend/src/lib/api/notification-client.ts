import { apiClient } from './client';
import { Notification } from '@/types/notification';

export const notificationClient = {
  getNotifications: async (): Promise<Notification[]> => {
    return apiClient.get<Notification[]>('/api/v1/notifications');
  },
};
