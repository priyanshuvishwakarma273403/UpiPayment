import instance from './axios';

export const transactionApi = {
  getUserTransactions: async (userId, page = 0, size = 5) => {
    const response = await instance.get(`/transactions/user/${userId}?page=${page}&size=${size}`);
    return response.data;
  },

  getMerchantTransactions: async (merchantId) => {
    const response = await instance.get(`/transactions/merchant/${merchantId}`);
    return response.data;
  }
};

export default transactionApi;
