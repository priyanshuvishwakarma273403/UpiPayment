"use client";

import { createContext, useContext, useState, useEffect, type ReactNode } from "react";
import { authStore, type UserSession } from "@/stores/authStore";

interface AuthContextType {
  user: UserSession | null;
  isAuthenticated: boolean;
  login: (token: string, refreshToken: string | undefined, user: UserSession) => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  isAuthenticated: false,
  login: () => {},
  logout: () => {},
  hasRole: () => false,
  hasAnyRole: () => false,
});

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSession | null>(null);

  useEffect(() => {
    const existingUser = authStore.getUser();
    if (existingUser) {
      setUser(existingUser);
    }
  }, []);

  const login = (token: string, refreshToken: string | undefined, userSession: UserSession) => {
    authStore.setToken(token, refreshToken);
    authStore.setUser(userSession);
    setUser(userSession);
  };

  const logout = () => {
    authStore.clearToken();
    setUser(null);
  };

  const hasRole = (role: string): boolean => {
    if (!user) return false;
    const userRoles = user.roles || (user.role ? [user.role] : []);
    const normalizedTarget = role.replace(/^ROLE_/, "").toUpperCase();
    return userRoles.some((r) => r.replace(/^ROLE_/, "").toUpperCase() === normalizedTarget);
  };

  const hasAnyRole = (roles: string[]): boolean => {
    if (!user) return false;
    return roles.some((role) => hasRole(role));
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, logout, hasRole, hasAnyRole }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
