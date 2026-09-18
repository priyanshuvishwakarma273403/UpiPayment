import { create } from 'zustand';

interface AuthState {
  token: string | null;
  userId: string | null;
  upiId: string | null;
  setAuth: (token: string, userId: string, upiId: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  userId: null,
  upiId: null,
  setAuth: (token, userId, upiId) => set({ token, userId, upiId }),
  logout: () => set({ token: null, userId: null, upiId: null }),
}));
