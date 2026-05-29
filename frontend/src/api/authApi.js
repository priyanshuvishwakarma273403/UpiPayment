import instance from './axios';

export const authApi = {
  register: async (data) => {
    const response = await instance.post('/auth/register', data);
    return response.data;
  },
  
  login: async (data) => {
    const response = await instance.post('/auth/login', data);
    return response.data;
  },
  
  verifyOtp: async (data) => {
    const response = await instance.post('/auth/verify-otp', data);
    return response.data;
  },
  
  resendOtp: async (phone) => {
    const response = await instance.post('/auth/resend-otp', { phone });
    return response.data;
  }
};

export default authApi;
