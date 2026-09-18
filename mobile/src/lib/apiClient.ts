import axios from 'axios';
import { API_CONFIG } from '../constants/api';
import { SecureStorage } from './secureStorage';

export const apiClient = axios.create({
  baseURL: API_CONFIG.GATEWAY_URL,
  timeout: API_CONFIG.TIMEOUT_MS,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(async (config) => {
  const token = await SecureStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
