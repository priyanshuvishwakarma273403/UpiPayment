import { useState, useCallback } from 'react';

export function usePayment() {
  const [isProcessing, setIsProcessing] = useState(false);

  const initiatePayment = useCallback(async (payload: { receiverUpiId: string; amount: number }) => {
    setIsProcessing(true);
    // Boilerplate stub
    setIsProcessing(false);
  }, []);

  return { initiatePayment, isProcessing };
}
