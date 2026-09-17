import { apiClient } from './client';

export interface LoginPayload {
  email?: string;
  phone?: string;
  password: string;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  phoneNumber: string;
  password: string;
  upiId?: string;
}

export interface VerifyOtpPayload {
  phone: string;
  otp: string;
}

export interface ResendOtpPayload {
  phone: string;
}

export interface RefreshTokenPayload {
  refreshToken: string;
}

export interface UserInfo {
  id: number;
  name: string;
  email: string;
  upiId?: string;
  phone?: string;
  verified?: boolean;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType?: string;
  expiresIn?: number;
  userId?: number;
  email?: string;
  fullName?: string;
  upiId?: string;
  roles?: string;
  phoneNumber?: string;
  isPhoneVerified?: boolean;
  token?: string;
  user?: UserInfo;
}

export const authClient = {
  login: async (payload: LoginPayload): Promise<AuthResponse> => {
    return apiClient.post<AuthResponse>('/auth/login', payload);
  },
  register: async (payload: RegisterPayload): Promise<AuthResponse> => {
    return apiClient.post<AuthResponse>('/auth/register', payload);
  },
  verifyOtp: async (payload: VerifyOtpPayload): Promise<AuthResponse> => {
    return apiClient.post<AuthResponse>('/auth/verify-otp', payload);
  },
  resendOtp: async (payload: ResendOtpPayload): Promise<{ message: string }> => {
    return apiClient.post<{ message: string }>('/auth/resend-otp', payload);
  },
  refreshToken: async (token: string): Promise<AuthResponse> => {
    return apiClient.post<AuthResponse>('/auth/refresh-token', { refreshToken: token });
  },
  getUserById: async (id: number | string): Promise<UserInfo> => {
    return apiClient.get<UserInfo>(`/auth/users/${id}`);
  },
};
