import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowUp, ArrowDown, Filter, Plus, CreditCard, ChevronRight, Lock } from 'lucide-react';
import toast from 'react-hot-toast';

import { useAuthStore } from '../../store/authStore';
import { useWalletStore } from '../../store/walletStore';
import { useTransactions } from '../../hooks/useTransactions';
import { useBalance } from '../../hooks/useBalance';
import { useHaptic } from '../../hooks/useHaptic';

import TransactionItem from '../../components/payment/TransactionItem';
import TransactionDetail from '../../components/payment/TransactionDetail';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import TopBar from '../../components/layout/TopBar';
import { formatAmount } from '../../utils/formatAmount';

export const WalletScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const user = useAuthStore(state => state.user);

  // Queries
  const { balance } = useBalance(user?.id);
  const { data: allTransactions, isLoading, refetch } = useTransactions(user?.id, 30);

  const [selectedTxn, setSelectedTxn] = useState(null);
  const [activeFilter, setActiveFilter] = useState('ALL'); // 'ALL', 'SENT', 'RECEIVED', 'FAILED'
  const [pageSize, setPageSize] = useState(10); // local pagination sizing

  const handleTxnClick = (txn) => {
    triggerHaptic('light');
    setSelectedTxn(txn);
  };

  const handleAddMoney = () => {
    triggerHaptic('light');
    navigate('/wallet/add-money');
  };

  // Filter computation logic
  const getFilteredTransactions = () => {
    if (!allTransactions) return [];
    
    switch (activeFilter) {
      case 'SENT':
        return allTransactions.filter(t => t.type === 'DEBIT');
      case 'RECEIVED':
        return allTransactions.filter(t => t.type === 'CREDIT');
      case 'FAILED':
        return allTransactions.filter(t => t.status === 'FAILED');
      case 'ALL':
      default:
        return allTransactions;
    }
  };

  const filteredTxns = getFilteredTransactions();
  const displayedTxns = filteredTxns.slice(0, pageSize);

  const filterTabs = [
    { id: 'ALL', label: 'All' },
    { id: 'SENT', label: 'Sent' },
    { id: 'RECEIVED', label: 'Received' },
    { id: 'FAILED', label: 'Failed' }
  ];

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white">
      {/* Sticky Header */}
      <TopBar title="Transaction Ledger" showBack={false} />

      <div className="flex-1 p-5 overflow-y-auto no-scrollbar flex flex-col">
        {/* Large Available/Frozen Balance Card */}
        <Card variant="wallet" shine className="p-5 mb-6 flex flex-col justify-between">
          <div className="flex justify-between items-start">
            <div className="flex flex-col">
              <span className="text-[10px] font-bold text-white/50 tracking-widest uppercase mb-1">
                Available Wallet Balance
              </span>
              <span className="text-3xl font-extrabold tracking-tight amount-font text-white">
                {formatAmount(balance)}
              </span>
            </div>
            
            <button
              onClick={handleAddMoney}
              className="p-2.5 rounded-full bg-white/10 text-white border border-white/15 active:scale-90 transition-transform duration-100"
              aria-label="Quick Add Money"
            >
              <Plus className="w-5 h-5" />
            </button>
          </div>

          <div className="w-full h-px bg-white/5 my-4" />

          {/* Frozen / Reserved funds */}
          <div className="flex justify-between items-center text-xs">
            <div className="flex items-center gap-1.5 opacity-60">
              <Lock className="w-3.5 h-3.5 text-warning" />
              <span>Frozen Balance</span>
            </div>
            <span className="font-bold amount-font text-warning">{formatAmount(450.00)}</span>
          </div>
        </Card>

        {/* Filter Scroll Tabs */}
        <div className="flex gap-2.5 mb-5 overflow-x-auto no-scrollbar py-0.5">
          {filterTabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => { triggerHaptic('light'); setActiveTab(tab.id); setPageSize(10); }}
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

        {/* Ledger History List */}
        <div className="flex-1 flex flex-col">
          <div className="flex justify-between items-center mb-3">
            <h3 className="text-[10px] font-bold text-textSecondary uppercase tracking-widest">
              Ledger Statements ({filteredTxns.length})
            </h3>
          </div>

          {isLoading ? (
            <div className="flex flex-col gap-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="h-16 w-full shimmer-bg rounded-2xl animate-pulse" />
              ))}
            </div>
          ) : displayedTxns.length > 0 ? (
            <div className="flex flex-col gap-3">
              {displayedTxns.map((txn) => (
                <TransactionItem
                  key={txn.id}
                  txn={txn}
                  onClick={() => handleTxnClick(txn)}
                />
              ))}
              
              {/* Load More Pagination indicator */}
              {filteredTxns.length > pageSize && (
                <button
                  onClick={() => { triggerHaptic('light'); setPageSize(prev => prev + 10); }}
                  className="w-full py-3.5 rounded-2xl bg-white/[0.02] border border-white/[0.04] text-xs font-semibold text-textSecondary active:bg-white/[0.05] active:text-white transition-colors text-center mt-2"
                >
                  Load More Transactions
                </button>
              )}
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center py-12 text-center my-auto">
              <div className="p-4 bg-white/[0.02] border border-white/5 rounded-[22px] mb-4 text-textSecondary">
                <Filter className="w-8 h-8" />
              </div>
              <h4 className="text-sm font-bold text-white tracking-wide">No statement records</h4>
              <p className="text-xs text-textSecondary mt-1 px-8 leading-snug">
                We couldn't find any transactions fitting the {activeFilter.toLowerCase()} criteria.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Invoice Modal Overlay */}
      <TransactionDetail
        txn={selectedTxn}
        isOpen={!!selectedTxn}
        onClose={() => setSelectedTxn(null)}
      />
    </div>
  );
};

export default WalletScreen;
