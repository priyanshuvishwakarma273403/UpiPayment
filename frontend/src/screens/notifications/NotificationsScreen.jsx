import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Bell, Info, CreditCard, Sparkles, AlertTriangle, Trash2, CheckSquare } from 'lucide-react';
import toast from 'react-hot-toast';

import { useHaptic } from '../../hooks/useHaptic';
import TopBar from '../../components/layout/TopBar';

export const NotificationsScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();

  const [activeFilter, setActiveFilter] = useState('ALL'); // 'ALL', 'PAYMENTS', 'ALERTS', 'OFFERS'
  const [list, setList] = useState([
    {
      id: 'n1',
      type: 'PAYMENTS',
      title: 'Payment Received',
      body: '₹1,500.00 credited from Ishita Patel (UPI ID: ishita@upimesh).',
      time: new Date(Date.now() - 1000 * 60 * 3).toISOString(), // 3m ago
      isUnread: true,
      icon: CreditCard,
      color: 'text-success bg-success/15 border-success/20'
    },
    {
      id: 'n2',
      type: 'ALERTS',
      title: 'Payment Flagged Guard Alert',
      body: 'Attempted payment of ₹5,000 to user suspicious@upi was flagged as risky.',
      time: new Date(Date.now() - 1000 * 60 * 45).toISOString(), // 45m ago
      isUnread: true,
      icon: AlertTriangle,
      color: 'text-danger bg-danger/15 border-danger/20',
      action: () => navigate('/fraud-alert')
    },
    {
      id: 'n3',
      type: 'OFFERS',
      title: 'Grab Flat ₹50 Cashback',
      body: 'Get guaranteed cashback on your first QR scan this week. Try Scan & Pay!',
      time: new Date(Date.now() - 1000 * 60 * 180).toISOString(), // 3h ago
      isUnread: false,
      icon: Sparkles,
      color: 'text-[#FFD93D] bg-[#FFD93D]/15 border-[#FFD93D]/20',
      action: () => navigate('/scan')
    },
    {
      id: 'n4',
      type: 'PAYMENTS',
      title: 'Wallet Top Up Complete',
      body: 'Successfully added ₹2,000.00 to your wallet account via HDFC Bank.',
      time: new Date(Date.now() - 1000 * 60 * 1440).toISOString(), // 1d ago
      isUnread: false,
      icon: CreditCard,
      color: 'text-secondary bg-secondary/15 border-secondary/20'
    }
  ]);

  const handleMarkAllRead = () => {
    triggerHaptic('medium');
    setList(prev => prev.map(n => ({ ...n, isUnread: false })));
    toast.success('All alerts marked as read');
  };

  const handleDismiss = (id) => {
    triggerHaptic('light');
    setList(prev => prev.filter(n => n.id !== id));
  };

  const getFilteredList = () => {
    if (activeFilter === 'ALL') return list;
    return list.filter(n => n.type === activeFilter);
  };

  const filteredList = getFilteredList();

  const filterTabs = [
    { id: 'ALL', label: 'All' },
    { id: 'PAYMENTS', label: 'Payments' },
    { id: 'ALERTS', label: 'Alerts' },
    { id: 'OFFERS', label: 'Offers' }
  ];

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white select-none">
      {/* Sticky Header with Mark Read action */}
      <TopBar
        title="Alert Notifications"
        rightAction={
          list.some(n => n.isUnread) && (
            <button
              onClick={handleMarkAllRead}
              className="p-2 rounded-full hover:bg-white/5 text-secondary flex items-center gap-1 active:scale-95 transition-transform"
              title="Mark all read"
            >
              <CheckSquare className="w-4.5 h-4.5" />
            </button>
          )
        }
      />

      <div className="flex-1 p-5 overflow-y-auto no-scrollbar flex flex-col">
        {/* Filters bar */}
        <div className="flex gap-2.5 mb-5 overflow-x-auto no-scrollbar py-0.5">
          {filterTabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => { triggerHaptic('light'); setActiveFilter(tab.id); }}
              className={`px-4.5 py-2 text-xs font-bold rounded-xl border transition-all duration-150 flex-shrink-0 ${
                activeFilter === tab.id
                  ? 'bg-primary text-white border-primary/20 shadow-glow-primary'
                  : 'bg-[#12121A] text-textSecondary border-white/5 hover:text-white'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* List Frame */}
        <div className="flex-1">
          <AnimatePresence mode="popLayout">
            {filteredList.length > 0 ? (
              <div className="flex flex-col gap-3">
                {filteredList.map((item) => {
                  const Svg = item.icon;
                  return (
                    <motion.div
                      key={item.id}
                      initial={{ scale: 0.95, opacity: 0 }}
                      animate={{ scale: 1, opacity: 1 }}
                      exit={{ x: '-100%', opacity: 0 }}
                      transition={{ type: 'spring', stiffness: 350, damping: 25 }}
                      onClick={() => item.action && item.action()}
                      className={`p-4 rounded-2xl border flex gap-3.5 relative overflow-hidden transition-colors cursor-pointer select-none ${
                        item.isUnread
                          ? 'bg-[#12121A] border-white/10 shadow-glass'
                          : 'bg-[#12121A]/40 border-white/[0.04] opacity-80'
                      }`}
                    >
                      {/* Unread circle badge */}
                      {item.isUnread && (
                        <span className="absolute top-4 right-4 w-2 h-2 rounded-full bg-primary" />
                      )}

                      {/* Icon */}
                      <div className={`p-2.5 rounded-xl border flex-shrink-0 h-10 w-10 flex items-center justify-center ${item.color}`}>
                        <Svg className="w-4.5 h-4.5" />
                      </div>

                      {/* Content details */}
                      <div className="flex-1 text-left">
                        <h4 className="text-xs font-bold text-white tracking-wide pr-3">{item.title}</h4>
                        <p className="text-[11px] text-textSecondary mt-1.5 leading-snug">{item.body}</p>
                        
                        <div className="flex justify-between items-center mt-3 text-[9px] font-bold text-textSecondary amount-font">
                          <span>{item.type}</span>
                          <span>{new Date(item.time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                        </div>
                      </div>

                      {/* Action dismiss */}
                      <button
                        onClick={(e) => { e.stopPropagation(); handleDismiss(item.id); }}
                        className="p-1 rounded-full text-textSecondary/30 hover:text-danger hover:bg-danger/10 transition-colors self-start"
                        aria-label="Dismiss Alert"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </motion.div>
                  );
                })}
              </div>
            ) : (
              <div className="flex-1 flex flex-col items-center justify-center py-16 text-center my-auto">
                <div className="p-4 bg-white/[0.01] border border-white/5 rounded-[22px] mb-4 text-textSecondary">
                  <Bell className="w-8 h-8 opacity-45" />
                </div>
                <h4 className="text-sm font-bold text-white tracking-wide">No alerts found</h4>
                <p className="text-xs text-textSecondary mt-1 px-8 leading-snug">
                  You are all caught up! There are no {activeFilter.toLowerCase()} alerts in your feed.
                </p>
              </div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
};

export default NotificationsScreen;
