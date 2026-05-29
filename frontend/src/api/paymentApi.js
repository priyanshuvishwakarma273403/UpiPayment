import instance from './axios';

export const paymentApi = {
  pay: async (data) => {
    const response = await instance.post('/payment/pay', data);
    return response.data;
  },

  offlinePay: async (data) => {
    const response = await instance.post('/payment/offline-pay', data);
    return response.data;
  },

  verifyPayment: async (paymentId) => {
    const response = await instance.post('/payment/verify', { paymentId });
    return response.data;
  },

  getPaymentDetail: async (paymentId) => {
    const response = await instance.get(`/payment/${paymentId}`);
    return response.data;
  },

  getPendingSync: async () => {
    const response = await instance.get('/payment/pending-sync');
    return response.data;
  }
};

export default paymentApi;
