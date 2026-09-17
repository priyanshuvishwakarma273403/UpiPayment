"use client";

export interface UserSession {
  id: string | number;
  username: string;
  email?: string;
  role: string;
  roles?: string[];
  upiId?: string;
  phone?: string;
}

export const authStore = {
  getToken: (): string | null => {
    if (typeof window !== "undefined") {
      return localStorage.getItem("sentinelx_token");
    }
    return null;
  },
  setToken: (token: string, refreshToken?: string): void => {
    if (typeof window !== "undefined") {
      localStorage.setItem("sentinelx_token", token);
      if (refreshToken) {
        localStorage.setItem("sentinelx_refresh_token", refreshToken);
      }
    }
  },
  getRefreshToken: (): string | null => {
    if (typeof window !== "undefined") {
      return localStorage.getItem("sentinelx_refresh_token");
    }
    return null;
  },
  clearToken: (): void => {
    if (typeof window !== "undefined") {
      localStorage.removeItem("sentinelx_token");
      localStorage.removeItem("sentinelx_refresh_token");
      localStorage.removeItem("sentinelx_user");
    }
  },
  getUser: (): UserSession | null => {
    if (typeof window !== "undefined") {
      const raw = localStorage.getItem("sentinelx_user");
      return raw ? JSON.parse(raw) : null;
    }
    return null;
  },
  setUser: (user: UserSession): void => {
    if (typeof window !== "undefined") {
      localStorage.setItem("sentinelx_user", JSON.stringify(user));
    }
  },
};
