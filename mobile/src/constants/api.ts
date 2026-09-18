/**
 * API Routing & Microservice Endpoints Constants
 */

export const API_CONFIG = {
  GATEWAY_URL: process.env.EXPO_PUBLIC_GATEWAY_URL || 'http://10.0.2.2:8080', // Android emulator default
  ML_SERVICE_URL: process.env.EXPO_PUBLIC_ML_SERVICE_URL || 'http://10.0.2.2:8000',
  TIMEOUT_MS: 15000,
  ENDPOINTS: {
    AUTH: {
      LOGIN: '/auth/login',
      REGISTER: '/auth/register',
      VERIFY_OTP: '/auth/verify-otp',
      REFRESH: '/auth/refresh-token',
    },
    WALLET: {
      BALANCE: (userId: string) => `/wallet/balance/${userId}`,
      ADD_MONEY: '/wallet/add-money',
    },
    PAYMENT: {
      PAY: '/payment/pay',
      OFFLINE_PAY: '/payment/offline-pay',
      VERIFY: '/payment/verify',
    },
    TRANSACTIONS: {
      USER: (userId: string) => `/transactions/user/${userId}`,
    },
    KYC: {
      INITIATE_AADHAAR: '/api/v1/kyc/initiate-aadhaar',
      VERIFY_AADHAAR: '/api/v1/kyc/verify-aadhaar',
      VERIFY_PAN: '/api/v1/kyc/verify-pan',
      FACE_MATCH: '/api/v1/kyc/face-match',
    },
    SYNC: {
      PROCESS: '/sync/process',
    },
  },
};
