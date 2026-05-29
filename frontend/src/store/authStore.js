import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set) => ({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      registeredUsers: [
        {
          id: 'u_default',
          name: 'Vikram Singh',
          email: 'vikram@upimesh',
          phone: '9876543210',
          password: 'password',
          upiId: '9876543210@upimesh',
          verified: true,
          roles: ['ROLE_USER', 'ROLE_MERCHANT']
        }
      ],

      registerLocalUser: (newUser) => set((state) => {
        const exists = state.registeredUsers.some(
          u => u.phone === newUser.phone || u.email === newUser.email
        );
        if (exists) return {};
        
        const addedUser = {
          ...newUser,
          id: `u_${Date.now()}`,
          upiId: `${newUser.phone}@upimesh`,
          verified: true,
          roles: ['ROLE_USER']
        };

        return {
          registeredUsers: [...state.registeredUsers, addedUser]
        };
      }),
      
      login: (user, token, refreshToken) => set({
        user: {
          id: user.id,
          name: user.name,
          email: user.email,
          upiId: user.upiId || `${user.phone}@upimesh`,
          roles: user.roles || ['ROLE_USER'],
          phone: user.phone
        },
        token,
        refreshToken,
        isAuthenticated: true
      }),
      
      logout: () => set({
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false
      }),
      
      updateToken: (token, refreshToken) => set((state) => ({
        token,
        refreshToken: refreshToken || state.refreshToken
      })),
      
      updateProfile: (profileData) => set((state) => ({
        user: state.user ? { ...state.user, ...profileData } : null
      }))
    }),
    {
      name: 'upimesh-auth',
    }
  )
);

export default useAuthStore;
