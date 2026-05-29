import instance from './axios';

export const aiApi = {
  chat: async (sessionId, message) => {
    const response = await instance.post('/ai/chat', { sessionId, message });
    return response.data;
  },

  getExpenseAnalysis: async (userId, timeframe = 'month') => {
    const response = await instance.post('/ai/expense-analysis', { userId, timeframe });
    return response.data;
  },

  explainFraud: async (paymentId) => {
    const response = await instance.post('/ai/fraud-explain', { paymentId });
    return response.data;
  }
};

export default aiApi;
