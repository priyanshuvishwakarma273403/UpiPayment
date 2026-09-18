import { useCallback } from 'react';
import * as LocalAuthentication from 'expo-local-authentication';

export function useBiometrics() {
  const authenticate = useCallback(async (promptMessage = 'Authenticate to Authorize Payment') => {
    try {
      const hasHardware = await LocalAuthentication.hasHardwareAsync();
      if (!hasHardware) return false;

      const isEnrolled = await LocalAuthentication.isEnrolledAsync();
      if (!isEnrolled) return false;

      const result = await LocalAuthentication.authenticateAsync({
        promptMessage,
        fallbackLabel: 'Enter MPIN',
      });

      return result.success;
    } catch {
      return false;
    }
  }, []);

  return { authenticate };
}
