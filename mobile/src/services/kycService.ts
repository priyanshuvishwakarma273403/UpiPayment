import { apiClient } from '../lib/apiClient';
import { API_CONFIG } from '../constants/api';

export const kycService = {
  initiateAadhaarOtp: async (aadhaarEncrypted: string) => {
    // Boilerplate stub
  },
  verifyAadhaarOtp: async (otp: string) => {
    // Boilerplate stub
  },
  verifyPan: async (panEncrypted: string) => {
    // Boilerplate stub
  },
  faceMatch: async (imageUri: string) => {
    // Boilerplate stub
  },
};
