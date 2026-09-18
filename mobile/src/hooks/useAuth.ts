import { useState, useCallback } from 'react';

export function useAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<{ id: string; upiId: string; name: string } | null>(null);

  const login = useCallback(async (credentials: { email: string; pass: string }) => {
    // Boilerplate stub
  }, []);

  const logout = useCallback(async () => {
    // Boilerplate stub
  }, []);

  return { isAuthenticated, user, login, logout };
}
