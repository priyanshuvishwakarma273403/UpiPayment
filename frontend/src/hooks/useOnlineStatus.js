import { useEffect } from 'react';
import { useOfflineStore } from '../store/offlineStore';

export const useOnlineStatus = () => {
  const setOnlineStatus = useOfflineStore((state) => state.setOnlineStatus);
  const isOnline = useOfflineStore((state) => state.isOnline);

  useEffect(() => {
    const handleOnline = () => setOnlineStatus(true);
    const handleOffline = () => setOnlineStatus(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    
    // Set initial state
    setOnlineStatus(navigator.onLine);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, [setOnlineStatus]);

  return isOnline;
};

export default useOnlineStatus;
