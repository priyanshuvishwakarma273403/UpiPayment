import axios from 'axios';
import toast from 'react-hot-toast';
import { useAuthStore } from '../store/authStore';

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10s timeout
});

// Request Interceptor to inject JWT token
instance.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Variables for managing concurrent token refreshes
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Response Interceptor for handling 401 (token refresh) and error toast messages
instance.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // Check if offline (no response received)
    if (!error.response) {
      // Return a rejected promise but don't toast if offline, as the AppShell handles offline banner
      return Promise.reject(error);
    }

    const status = error.response.status;

    // Trigger Token Refresh logic if 401 Unauthorized occurs
    if (status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return instance(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = useAuthStore.getState().refreshToken;
      if (!refreshToken) {
        useAuthStore.getState().logout();
        isRefreshing = false;
        return Promise.reject(error);
      }

      try {
        const response = await axios.post(`${instance.defaults.baseURL}/auth/refresh-token`, {
          refreshToken,
        });

        const { token: newAccessToken, refreshToken: newRefreshToken } = response.data;
        useAuthStore.getState().updateToken(newAccessToken, newRefreshToken || refreshToken);
        
        processQueue(null, newAccessToken);
        isRefreshing = false;

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return instance(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        useAuthStore.getState().logout();
        isRefreshing = false;
        toast.error('Your session expired. Please log in again.');
        return Promise.reject(refreshError);
      }
    }

    // Standard error toast for generic errors
    const errorMessage = error.response?.data?.message || error.response?.data?.error || error.message || 'Network request failed';
    
    // Do not show general network error toasts for auth screens, which handle their own validation errors
    const skipToastUrls = ['/auth/login', '/auth/register', '/auth/verify-otp'];
    const shouldSkip = skipToastUrls.some(url => originalRequest.url?.includes(url));
    
    if (!shouldSkip) {
      toast.error(errorMessage);
    }

    return Promise.reject(error);
  }
);

export default instance;
