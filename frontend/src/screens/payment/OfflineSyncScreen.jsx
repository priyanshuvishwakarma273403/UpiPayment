import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Wifi, WifiOff, RefreshCw, Trash2, ArrowLeft, CheckCircle, Clock } from 'lucide-react';
import toast from 'react-hot-toast';

import { useOfflineStore } from '../../store/offlineStore';
import { useHaptic } from '../../hooks/useHaptic';
import { formatAmount } from '../../utils/formatAmount';
import { formatDate } from '../../utils/formatDate';

import TopBar from '../../components/layout/TopBar';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';

export const OfflineSyncScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();

  const isOnline = useOfflineStore(state => state.isOnline);
  const pendingPayments = useOfflineStore(state => state.pendingPayments);
  const removePendingPayment = useOfflineStore(state => state.removePendingPayment);
  const syncPendingPayments = useOfflineStore(state => state.syncPendingPayments);
  const isSyncing = useOfflineStore(state => state.isSyncing);

  const handleSyncNow = async () => {
    if (!isOnline) {
      toast.error('Cannot sync while offline. Please connect to a network.');
      return;
    }
    triggerHaptic('medium');
    try {
      await syncPendingPayments();
      navigate('/home');
    } catch (e) {
      console.error(e);
    }
  };

  const handleDeleteQueued = (id) => {
    triggerHaptic('heavy');
    removePendingPayment(id);
    toast.success('Queued payment cancelled successfully');
  };

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white select-none">
      {/* Header */}
      <TopBar title="Offline Queue Manager" showBack={true} onBack={() => navigate('/home')} />

      <div className="flex-1 p-5 overflow-y-auto no-scrollbar flex flex-col justify-between">
        
        <div className="flex flex-col gap-5 flex-1">
          {/* Connectivity Status Banner */}
          <div className={`p-4 rounded-2xl flex justify-between items-center border ${
            isOnline 
              ? 'bg-success/10 border-success/20 text-success' 
              : 'bg-orange-500/10 border-orange-500/20 text-orange-400'
          }`}>
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-xl bg-white/5 flex items-center justify-center">
                {isOnline ? <Wifi className="w-5 h-5" /> : <WifiOff className="w-5 h-5 animate-pulse" />}
              </div>
              <div className="flex flex-col text-left">
                <span className="text-xs font-bold text-white tracking-wide">
                  {isOnline ? 'Connection Restored' : 'Operating Offline'}
                </span>
                <span className="text-[10px] text-textSecondary mt-0.5">
                  {isOnline ? 'Ready to synchronize ledger statement' : 'Payments will queue locally'}
                </span>
              </div>
            </div>
          </div>

          {/* Queue Count */}
          <div className="flex justify-between items-center mt-2 mb-1">
            <h4 className="text-[10px] font-bold text-textSecondary uppercase tracking-widest pl-1">
              Queued Payments ({pendingPayments.length})
            </h4>
          </div>

          {/* Pending items list */}
          {pendingPayments.length > 0 ? (
            <div className="flex flex-col gap-3.5">
              {pendingPayments.map((item) => (
                <div
                  key={item.id}
                  className="p-4 rounded-2xl bg-white/[0.01] border border-white/[0.04] flex items-center justify-between"
                >
                  <div className="flex items-center gap-3 text-left">
                    <div className="w-10 h-10 rounded-full bg-orange-500/15 border border-orange-500/20 flex items-center justify-center text-orange-400">
                      <Clock className="w-5 h-5 animate-pulse" />
                    </div>
                    <div>
                      <h5 className="text-xs font-bold text-white tracking-wide">{item.contactName}</h5>
                      <p className="text-[10px] text-textSecondary amount-font mt-0.5">{item.upiId}</p>
                      <p className="text-[9px] text-textSecondary/50 mt-1">{formatDate(item.timestamp)}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-3">
                    <span className="text-xs font-extrabold text-white amount-font">
                      {formatAmount(item.amount)}
                    </span>
                    <button
                      onClick={() => handleDeleteQueued(item.id)}
                      className="p-2 rounded-xl text-textSecondary/40 hover:text-danger hover:bg-danger/10 transition-colors"
                      aria-label="Delete queued item"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center py-16 text-center my-auto">
              <div className="p-4 bg-white/[0.01] border border-white/5 rounded-[22px] mb-4 text-textSecondary">
                <CheckCircle className="w-8 h-8 opacity-45" />
              </div>
              <h4 className="text-sm font-bold text-white tracking-wide">Queue is empty</h4>
              <p className="text-xs text-textSecondary mt-1 px-8 leading-snug">
                All offline transactions have been synchronized successfully.
              </p>
            </div>
          )}
        </div>

        {/* Sync proceed action */}
        {pendingPayments.length > 0 && (
          <div className="mt-6">
            <Button
              onClick={handleSyncNow}
              disabled={!isOnline || isSyncing}
              variant="gradient"
              className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2"
            >
              <RefreshCw className={`w-4 h-4 ${isSyncing ? 'animate-spin' : ''}`} />
              {isSyncing ? 'Syncing Queue...' : `Sync ${pendingPayments.length} Payment(s)`}
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};

export default OfflineSyncScreen;
