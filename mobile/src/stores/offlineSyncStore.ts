import { create } from 'zustand';

interface OfflineSyncState {
  pendingTransactions: any[];
  addPendingTransaction: (tx: any) => void;
  clearQueue: () => void;
}

export const useOfflineSyncStore = create<OfflineSyncState>((set) => ({
  pendingTransactions: [],
  addPendingTransaction: (tx) =>
    set((state) => ({ pendingTransactions: [...state.pendingTransactions, tx] })),
  clearQueue: () => set({ pendingTransactions: [] }),
}));
