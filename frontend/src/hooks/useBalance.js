import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import instance from '../api/axios';
import { useWalletStore } from '../store/walletStore';

export const useBalance = (userId) => {
  const setBalance = useWalletStore((state) => state.setBalance);
  const queryClient = useQueryClient();

  const balanceQuery = useQuery({
    queryKey: ['balance', userId],
    queryFn: async () => {
      if (!userId) return 0;
      const response = await instance.get(`/wallet/balance/${userId}`);
      const amount = Number(response.data.balance !== undefined ? response.data.balance : response.data);
      setBalance(amount);
      return amount;
    },
    enabled: !!userId,
    staleTime: 15000, // 15 seconds fresh state
    refetchOnWindowFocus: true,
  });

  const addMoneyMutation = useMutation({
    mutationFn: async ({ amount }) => {
      const response = await instance.post('/wallet/add-money', { userId, amount });
      return response.data;
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['balance', userId] });
      queryClient.invalidateQueries({ queryKey: ['transactions', userId] });
      if (data && data.balance !== undefined) {
        setBalance(Number(data.balance));
      }
    }
  });

  return {
    balance: balanceQuery.data ?? useWalletStore.getState().balance,
    isLoading: balanceQuery.isLoading,
    isRefetching: balanceQuery.isRefetching,
    refetch: balanceQuery.refetch,
    addMoney: addMoneyMutation.mutateAsync,
    isAddingMoney: addMoneyMutation.isPending
  };
};

export default useBalance;
