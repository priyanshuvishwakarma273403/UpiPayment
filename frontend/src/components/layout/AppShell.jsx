import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import StatusBar from './StatusBar';
import BottomNav from './BottomNav';
import { useOnlineStatus } from '../../hooks/useOnlineStatus';
import { useOfflineStore } from '../../store/offlineStore';
import { WifiOff, AlertTriangle } from 'lucide-react';

export const AppShell = ({ children }) => {
  const isOnline = useOnlineStatus();
  const location = useLocation();
  const pendingPayments = useOfflineStore((state) => state.pendingPayments);
  const syncPendingPayments = useOfflineStore((state) => state.syncPendingPayments);
  const isSyncing = useOfflineStore((state) => state.isSyncing);

  const currentPath = location.pathname;
  
  // Decide whether to show the bottom navigation
  const showNav = ['/home', '/wallet', '/scan', '/ai/chat', '/profile'].includes(currentPath);

  // Synchronize queued transactions if transitioning back online
  useEffect(() => {
    if (isOnline && pendingPayments.length > 0 && !isSyncing) {
      syncPendingPayments().catch(err => console.error("Auto sync failed:", err));
    }
  }, [isOnline, pendingPayments.length, isSyncing, syncPendingPayments]);

  return (
    <div className="min-h-screen w-full bg-slate-950 flex items-center justify-center p-0 md:p-6 select-none font-sans">
      {/* Outer mock mobile phone frame (styled to simulate an iPhone 14 Pro Max frame on desktop viewports) */}
      <div className="relative w-full max-w-[430px] h-[100vh] md:h-[880px] bg-[#0A0A0F] md:rounded-[40px] md:border-[10px] md:border-slate-800 shadow-[0_25px_60px_-15px_rgba(0,0,0,0.8)] overflow-hidden flex flex-col">
        
        {/* Mock Notch (renders only on desktop layouts) */}
        <div className="hidden md:block absolute top-0 left-1/2 transform -translate-x-1/2 w-32 h-6 bg-black rounded-b-2xl z-50 pointer-events-none" />

        {/* Offline Status Warning Bar */}
        <AnimatePresence>
          {!isOnline && (
            <motion.div
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 40, opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              className="bg-orange-500 text-black text-xs font-bold flex items-center justify-center gap-2 z-50 sticky top-10 border-b border-orange-600 px-4"
            >
              <WifiOff className="w-4 h-4 animate-bounce" />
              <span>Offline - Payments will queue in wallet</span>
              {pendingPayments.length > 0 && (
                <span className="bg-black text-white px-2 py-0.5 rounded-full text-[10px] font-extrabold animate-pulse">
                  {pendingPayments.length} queued
                </span>
              )}
            </motion.div>
          )}
          {isOnline && isSyncing && (
            <motion.div
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 40, opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              className="bg-primary text-white text-xs font-bold flex items-center justify-center gap-2 z-50 sticky top-10 border-b border-primary/55 px-4"
            >
              <AlertTriangle className="w-4 h-4 animate-spin" />
              <span>Syncing offline payments...</span>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Top Mobile Status Bar - only on desktop/laptop */}
        <div className="hidden md:block">
          <StatusBar />
        </div>

        {/* Main Application Container */}
        <div className="flex-1 overflow-y-auto no-scrollbar relative flex flex-col bg-[#0A0A0F]">
          {children}
        </div>

        {/* Conditional Bottom Tab Navigation */}
        {showNav && <BottomNav />}

        {/* Mock Home Swipe Indicator (renders only on desktop layouts) */}
        <div className="hidden md:flex justify-center items-center h-4 bg-[#12121A] py-1">
          <div className="w-32 h-1 bg-white/20 rounded-full" />
        </div>
      </div>
    </div>
  );
};

export default AppShell;
