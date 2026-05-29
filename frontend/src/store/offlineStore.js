import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import instance from '../api/axios';
import toast from 'react-hot-toast';

export const useOfflineStore = create(
  persist(
    (set, get) => ({
      isOnline: typeof window !== 'undefined' ? window.navigator.onLine : true,
      pendingPayments: [],
      isSyncing: false,

      setOnlineStatus: (status) => {
        const wasOffline = !get().isOnline;
        set({ isOnline: status });
        
        if (status && wasOffline && get().pendingPayments.length > 0) {
          get().syncPendingPayments();
        }
      },

      addPendingPayment: (payment) => {
        const newPayment = {
          ...payment,
          id: payment.id || `off_${Date.now()}`,
          isOffline: true,
          timestamp: new Date().toISOString()
        };
        set((state) => ({
          pendingPayments: [...state.pendingPayments, newPayment]
        }));
        toast.success("No connection. Payment queued offline!", { duration: 4000 });
      },

      removePendingPayment: (id) => {
        set((state) => ({
          pendingPayments: state.pendingPayments.filter(p => p.id !== id)
        }));
      },

      syncPendingPayments: async () => {
        const { pendingPayments, isSyncing } = get();
        if (pendingPayments.length === 0 || isSyncing) return;

        set({ isSyncing: true });
        const toastId = toast.loading(`Syncing ${pendingPayments.length} offline payment(s)...`);

        try {
          // POST /sync/process to sync the offline queue
          const response = await instance.post('/sync/process', { payments: pendingPayments });
          
          set({ pendingPayments: [], isSyncing: false });
          toast.success("Offline payments synced successfully!", { id: toastId });
          
          // Refresh window if necessary or rely on queries updating
          return response.data;
        } catch (error) {
          console.error("Failed to sync offline payments:", error);
          set({ isSyncing: false });
          toast.error("Offline sync failed. Retrying when connection is stable.", { id: toastId });
          throw error;
        }
      }
    }),
    {
      name: 'upimesh-offline-queue',
      partialize: (state) => ({ pendingPayments: state.pendingPayments }) // only save queued payments in localStorage
    }
  )
);

export default useOfflineStore;
