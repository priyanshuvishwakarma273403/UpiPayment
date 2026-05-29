import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Plus, ArrowDownLeft, Wallet } from 'lucide-react';
import Card from '../ui/Card';
import { useWalletStore } from '../../store/walletStore';
import { useAuthStore } from '../../store/authStore';
import { formatAmount } from '../../utils/formatAmount';
import { useHaptic } from '../../hooks/useHaptic';

export const WalletCard = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  
  const balance = useWalletStore((state) => state.balance);
  const isBalanceVisible = useWalletStore((state) => state.isBalanceVisible);
  const toggleVisibility = useWalletStore((state) => state.toggleVisibility);
  const user = useAuthStore((state) => state.user);

  const [counter, setCounter] = useState(0);

  // Animate the balance counter when the card mounts or balance updates
  useEffect(() => {
    if (!isBalanceVisible) return;
    
    let start = 0;
    const end = balance;
    if (end <= 0) {
      setCounter(0);
      return;
    }

    const duration = 800; // ms
    const stepTime = 16; // ~60fps
    const steps = duration / stepTime;
    const increment = end / steps;

    const timer = setInterval(() => {
      start += increment;
      if (start >= end) {
        setCounter(end);
        clearInterval(timer);
      } else {
        setCounter(start);
      }
    }, stepTime);

    return () => clearInterval(timer);
  }, [balance, isBalanceVisible]);

  const handleToggle = (e) => {
    e.stopPropagation();
    triggerHaptic('medium');
    toggleVisibility();
  };

  const handleAddMoney = () => {
    triggerHaptic('light');
    navigate('/wallet/add-money');
  };

  const handleSendMoney = () => {
    triggerHaptic('light');
    navigate('/send');
  };

  return (
    <Card variant="wallet" shine className="p-5 flex flex-col justify-between h-48 select-none">
      <div className="flex justify-between items-start">
        <div className="flex flex-col">
          <span className="text-[10px] font-bold text-white/50 tracking-widest uppercase mb-1">
            Total Balance
          </span>
          <div className="flex items-center gap-2">
            <span className="text-2xl font-extrabold tracking-tight amount-font text-white">
              {isBalanceVisible ? formatAmount(counter) : '₹ ••••••'}
            </span>
            <button 
              type="button" 
              onClick={handleToggle}
              className="p-1 rounded-full text-white/40 hover:text-white transition-colors"
            >
              {isBalanceVisible ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
            </button>
          </div>
        </div>
        <div className="flex items-center gap-1 bg-white/10 border border-white/10 px-2 py-0.5 rounded-lg">
          <Wallet className="w-3.5 h-3.5 text-secondary" />
          <span className="text-[9px] font-bold tracking-wider text-white uppercase">Active</span>
        </div>
      </div>

      <div className="flex flex-col">
        <span className="text-[9px] text-white/40 font-bold uppercase tracking-wider mb-0.5">UPI ID</span>
        <span className="text-xs font-semibold text-white/90">
          {user?.upiId || '9876543210@upimesh'}
        </span>
      </div>

      <div className="flex gap-2">
        <button
          type="button"
          onClick={handleAddMoney}
          className="flex-1 py-2.5 bg-white/10 hover:bg-white/15 border border-white/15 rounded-xl text-xs font-bold text-white flex items-center justify-center gap-1.5 transition-transform duration-100 active:scale-[0.97]"
        >
          <Plus className="w-3.5 h-3.5" />
          Add Money
        </button>
        <button
          type="button"
          onClick={handleSendMoney}
          className="flex-1 py-2.5 bg-white text-[#0A0A0F] border border-white hover:bg-white/90 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 transition-transform duration-100 active:scale-[0.97]"
        >
          <ArrowDownLeft className="w-3.5 h-3.5 rotate-180" />
          Send Money
        </button>
      </div>
    </Card>
  );
};

export default WalletCard;
