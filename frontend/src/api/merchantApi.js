import instance from './axios';

export const merchantApi = {
  registerMerchant: async (data) => {
    const response = await instance.post('/merchant/register', data);
    return response.data;
  },

  getMerchantQr: async (merchantId) => {
    const response = await instance.get(`/merchant/qr/${merchantId}`);
    return response.data;
  },

  generateDynamicQr: async (data) => {
    const response = await instance.post('/merchant/qr/dynamic', data);
    return response.data;
  }
};

export default merchantApi;
