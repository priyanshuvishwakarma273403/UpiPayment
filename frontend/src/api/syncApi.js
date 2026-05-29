import instance from './axios';

export const syncApi = {
  syncOfflinePayments: async (payments) => {
    const response = await instance.post('/sync/process', { payments });
    return response.data;
  }
};

export default syncApi;
