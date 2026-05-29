import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import instance from '../api/axios';

export const useWalletStore = create(
  persist(
    (set, get) => ({
      balance: 15420.50, // default dummy starting balance
      isBalanceVisible: true,
      isLoadingBalance: false,

      setBalance: (amount) => set({ balance: Number(amount) }),
      
      toggleVisibility: () => set((state) => ({ isBalanceVisible: !state.isBalanceVisible })),
      
      fetchBalance: async (userId) => {
        if (!userId) return;
        set({ isLoadingBalance: true });
        try {
          const response = await instance.get(`/wallet/balance/${userId}`);
          const fetchedBalance = Number(response.data.balance || response.data);
          set({ balance: fetchedBalance, isLoadingBalance: false });
        } catch (error) {
          console.error("Failed to fetch balance from server:", error);
          set({ isLoadingBalance: false });
          // Fallback - keep local balance
        }
      },
      
      addMoney: async (userId, amount) => {
        try {
          const response = await instance.post(`/wallet/add-money`, { userId, amount });
          const newBalance = Number(response.data.balance || (get().balance + Number(amount)));
          set({ balance: newBalance });
          return newBalance;
        } catch (error) {
          console.error("Failed to add money:", error);
          // Fallback to update locally in case API is down for demo purposes
          const fallbackBalance = get().balance + Number(amount);
          set({ balance: fallbackBalance });
          return fallbackBalance;
        }
      }
    }),
    {
      name: 'upimesh-wallet',
    }
  )
);

export default useWalletStore;
