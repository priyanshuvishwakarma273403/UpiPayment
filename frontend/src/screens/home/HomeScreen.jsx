import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Search, ArrowRight, Share2, RefreshCw } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';

import { useAuthStore } from '../../store/authStore';
import { useWalletStore } from '../../store/walletStore';
import { useTransactions } from '../../hooks/useTransactions';
import { useBalance } from '../../hooks/useBalance';
import { useHaptic } from '../../hooks/useHaptic';

import WalletCard from '../../components/payment/WalletCard';
import QuickActions from '../../components/payment/QuickActions';
import TransactionItem from '../../components/payment/TransactionItem';
import TransactionDetail from '../../components/payment/TransactionDetail';
import Avatar from '../../components/ui/Avatar';
import Card from '../../components/ui/Card';

export const HomeScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const user = useAuthStore(state => state.user);

  // Sync server queries
  const { balance, refetch: refetchBalance, isLoading: isBalanceLoading } = useBalance(user?.id);
  const { data: recentTxns, refetch: refetchTxns, isLoading: isTxnsLoading } = useTransactions(user?.id, 5);

  const [selectedTxn, setSelectedTxn] = useState(null);
  const [searchVal, setSearchVal] = useState('');
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [greetText, setGreetText] = useState('Good Morning');

  // Set greeting text based on time of day
  useEffect(() => {
    const hr = new Date().getHours();
    if (hr < 12) setGreetText('Good Morning');
    else if (hr < 17) setGreetText('Good Afternoon');
    else setGreetText('Good Evening');
  }, []);

  const handlePullToRefresh = async () => {
    triggerHaptic('medium');
    setIsRefreshing(true);
    
    try {
      await Promise.all([refetchBalance(), refetchTxns()]);
      toast.success('Wallet data refreshed!');
    } catch (e) {
      console.error(e);
    } finally {
      setTimeout(() => setIsRefreshing(false), 800);
    }
  };

  const handleTxnClick = (txn) => {
    triggerHaptic('light');
    setSelectedTxn(txn);
  };

  const handleNotification = () => {
    triggerHaptic('light');
    navigate('/notifications');
  };

  const offers = [
    {
      id: 'o1',
      title: 'Cashback Guarantee!',
      desc: 'Get flat ₹50 cashback on your first scan & pay transaction.',
      gradient: 'from-[#6C63FF]/30 to-[#00D2FF]/5 border-primary/20',
      action: () => navigate('/scan')
    },
    {
      id: 'o2',
      title: 'Refer & Earn ₹50',
      desc: 'Invite your contacts to join UPI Mesh. Earn on their first pay.',
      gradient: 'from-[#00E676]/30 to-[#00B248]/5 border-success/20',
      action: () => {
        triggerHaptic('light');
        if (navigator.share) {
          navigator.share({
            title: 'UPI Mesh App invitation',
            text: 'Hey, join UPI Mesh for secure AI-powered UPI payments!'
          });
        } else {
          toast.success('Referral message copied!');
        }
      }
    }
  ];

  return (
    <div className="flex-1 flex flex-col p-5 bg-[#0A0A0F] relative select-none">
      
      {/* Pull To Refresh animation status */}
      <div 
        onClick={handlePullToRefresh}
        className="w-full flex justify-center items-center py-1 cursor-pointer"
      >
        <span className="text-[9px] font-bold text-textSecondary uppercase tracking-widest flex items-center gap-1.5 active:text-white">
          <RefreshCw className={`w-3 h-3 ${isRefreshing ? 'animate-spin text-primary' : ''}`} />
          {isRefreshing ? 'Syncing Ledger...' : 'Tap to Sync Balance'}
        </span>
      </div>

      {/* Main Header */}
      <div className="flex justify-between items-center mt-3 mb-5">
        <div className="flex items-center gap-3.5" onClick={() => navigate('/profile')}>
          <Avatar name={user?.name || 'User'} size="md" className="cursor-pointer" />
          <div className="flex flex-col">
            <span className="text-[10px] font-bold uppercase tracking-widest text-textSecondary">
              {greetText}
            </span>
            <span className="text-base font-extrabold text-white tracking-wide truncate max-w-[150px]">
              {user?.name || 'User'}
            </span>
          </div>
        </div>

        {/* Notifications and Alerts Indicator */}
        <button
          onClick={handleNotification}
          className="p-3 bg-white/[0.03] border border-white/5 hover:bg-white/10 active:scale-90 transition-transform rounded-2xl relative text-white"
        >
          <Bell className="w-5 h-5" />
          <span className="absolute top-2.5 right-2.5 w-2 h-2 bg-primary rounded-full animate-ping" />
          <span className="absolute top-2.5 right-2.5 w-2 h-2 bg-primary rounded-full" />
        </button>
      </div>

      {/* Visually stunning Search Bar with glass effect */}
      <div className="relative flex items-center mb-6">
        <div className="absolute left-4 text-textSecondary">
          <Search className="w-4 h-4" />
        </div>
        <input
          type="text"
          placeholder="Search contacts, UPI IDs, or numbers..."
          value={searchVal}
          onChange={(e) => setSearchVal(e.target.value)}
          onClick={() => navigate('/send')}
          className="w-full py-3.5 pl-11 pr-4 rounded-2xl bg-white/[0.03] border border-white/[0.04] text-xs text-white placeholder-textSecondary font-semibold select-none outline-none cursor-pointer focus:border-primary/50"
        />
      </div>

      {/* Wallet balance Overview component */}
      <div className="mb-6">
        <WalletCard />
      </div>

      {/* Horizontal actions rail */}
      <QuickActions />

      {/* Offers & Referrals slider section */}
      <div className="mb-6 mt-2">
        <div className="flex justify-between items-center mb-3">
          <h3 className="text-xs font-bold text-textSecondary uppercase tracking-widest">
            Offers & Updates
          </h3>
        </div>
        <div className="flex gap-4 overflow-x-auto no-scrollbar py-1">
          {offers.map((offer) => (
            <Card
              key={offer.id}
              onClick={offer.action}
              className={`flex-shrink-0 w-72 p-4 border bg-gradient-to-tr ${offer.gradient}`}
              animate={false}
            >
              <h4 className="text-sm font-black text-white">{offer.title}</h4>
              <p className="text-[11px] text-textSecondary mt-1 leading-snug">{offer.desc}</p>
              <div className="flex items-center gap-1 mt-4 text-[10px] font-bold text-secondary">
                <span>View details</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </div>
            </Card>
          ))}
        </div>
      </div>

      {/* Recent transfers ledger */}
      <div className="mb-4">
        <div className="flex justify-between items-center mb-3.5">
          <h3 className="text-xs font-bold text-textSecondary uppercase tracking-widest">
            Recent Transactions
          </h3>
          <span
            onClick={() => { triggerHaptic('light'); navigate('/wallet'); }}
            className="text-xs font-bold text-secondary cursor-pointer hover:underline"
          >
            See All
          </span>
        </div>

        {/* Transactions List with custom skeleton states */}
        {isTxnsLoading ? (
          <div className="flex flex-col gap-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="h-16 w-full shimmer-bg rounded-2xl" />
            ))}
          </div>
        ) : recentTxns && recentTxns.length > 0 ? (
          <div className="flex flex-col gap-3">
            {recentTxns.map((txn) => (
              <TransactionItem
                key={txn.id}
                txn={txn}
                onClick={() => handleTxnClick(txn)}
              />
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-6 bg-white/[0.01] border border-dashed border-white/5 rounded-2xl text-center">
            <span className="text-[11px] text-textSecondary font-bold uppercase tracking-wider">No recent payments</span>
          </div>
        )}
      </div>

      {/* Detailed invoice drawer */}
      <TransactionDetail
        txn={selectedTxn}
        isOpen={!!selectedTxn}
        onClose={() => setSelectedTxn(null)}
      />
    </div>
  );
};

export default HomeScreen;
