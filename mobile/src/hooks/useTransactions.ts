import { useState, useCallback } from 'react';

export function useTransactions() {
  const [transactions, setTransactions] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const fetchTransactions = useCallback(async () => {
    setIsLoading(true);
    // Boilerplate stub
    setIsLoading(false);
  }, []);

  return { transactions, isLoading, fetchTransactions };
}
