import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Home, History, Scan, Bot, User } from 'lucide-react';
import { motion } from 'framer-motion';
import { useHaptic } from '../../hooks/useHaptic';
import { useOfflineStore } from '../../store/offlineStore';

export const BottomNav = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const triggerHaptic = useHaptic();
  const pendingPayments = useOfflineStore(state => state.pendingPayments);
  
  const currentPath = location.pathname;

  const tabs = [
    { name: 'Home', icon: Home, path: '/home' },
    { name: 'History', icon: History, path: '/wallet' },
    { name: 'Scan', icon: Scan, path: '/scan', isCenter: true },
    { name: 'AI Chat', icon: Bot, path: '/ai/chat' },
    { name: 'Profile', icon: User, path: '/profile' }
  ];

  const handleTabClick = (path) => {
    triggerHaptic('light');
    navigate(path);
  };

  return (
    <div className="h-16 bg-[#12121A]/95 border-t border-white/[0.05] flex items-center justify-around relative px-2 safe-bottom z-40 select-none backdrop-blur-lg">
      {tabs.map((tab) => {
        const isActive = currentPath === tab.path;
        const Icon = tab.icon;

        if (tab.isCenter) {
          return (
            <div key={tab.path} className="relative -top-5 w-16 h-16 flex items-center justify-center">
              <button
                onClick={() => handleTabClick(tab.path)}
                className="w-14 h-14 rounded-full bg-btn-grad hover:brightness-110 active:scale-90 transition-transform duration-100 flex items-center justify-center text-white shadow-glow-primary border border-white/20 relative"
                aria-label="Scan QR Code"
              >
                <Icon className="w-6 h-6 text-white" />
                {pendingPayments.length > 0 && (
                  <span className="absolute -top-1.5 -right-1.5 w-5 h-5 rounded-full bg-orange-500 text-[10px] font-bold flex items-center justify-center text-white border border-[#12121A]">
                    {pendingPayments.length}
                  </span>
                )}
              </button>
            </div>
          );
        }

        return (
          <button
            key={tab.path}
            onClick={() => handleTabClick(tab.path)}
            className="flex flex-col items-center justify-center flex-1 py-1 relative text-xs font-medium focus:outline-none"
          >
            {/* Active Indicator Pill */}
            {isActive && (
              <motion.div
                layoutId="activeTabPill"
                className="absolute top-0 w-8 h-1 bg-primary rounded-full"
                transition={{ type: 'spring', stiffness: 380, damping: 30 }}
              />
            )}
            
            <Icon 
              className={`w-5 h-5 transition-colors duration-150 ${
                isActive ? 'text-primary' : 'text-textSecondary'
              }`} 
            />
            <span 
              className={`mt-1 text-[10px] tracking-wide transition-colors duration-150 ${
                isActive ? 'text-white font-semibold' : 'text-textSecondary'
              }`}
            >
              {tab.name}
            </span>
          </button>
        );
      })}
    </div>
  );
};

export default BottomNav;
