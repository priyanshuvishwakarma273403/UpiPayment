import { create } from 'zustand';

interface WalletState {
  balance: number;
  currency: string;
  isFrozen: boolean;
  setBalance: (balance: number) => void;
  setFrozen: (isFrozen: boolean) => void;
}

export const useWalletStore = create<WalletState>((set) => ({
  balance: 0,
  currency: 'INR',
  isFrozen: false,
  setBalance: (balance) => set({ balance }),
  setFrozen: (isFrozen) => set({ isFrozen }),
}));
