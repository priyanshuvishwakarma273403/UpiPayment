import instance from './axios';

export const walletApi = {
  getBalance: async (userId) => {
    const response = await instance.get(`/wallet/balance/${userId}`);
    return response.data;
  },
  
  addMoney: async (data) => {
    const response = await instance.post('/wallet/add-money', data);
    return response.data;
  },
  
  getWalletTransactions: async (userId, page = 0, size = 20) => {
    const response = await instance.get(`/wallet/transactions/${userId}?page=${page}&size=${size}`);
    return response.data;
  }
};

export default walletApi;
