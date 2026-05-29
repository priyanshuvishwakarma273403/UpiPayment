import React, { useRef } from 'react';
import Avatar from '../ui/Avatar';
import Badge from '../ui/Badge';
import { formatAmount } from '../../utils/formatAmount';
import { formatDate } from '../../utils/formatDate';
import { useHaptic } from '../../hooks/useHaptic';
import toast from 'react-hot-toast';

export const TransactionItem = ({ txn, onClick }) => {
  const triggerHaptic = useHaptic();
  const longPressTimer = useRef(null);
  const isLongPress = useRef(false);

  const isCredit = txn.type === 'CREDIT';

  // Handle long press simulation for both desktop and mobile touch
  const startPress = () => {
    isLongPress.current = false;
    longPressTimer.current = setTimeout(() => {
      isLongPress.current = true;
      triggerHaptic('heavy');
      
      // Copy Transaction ID to Clipboard
      if (txn.id) {
        navigator.clipboard.writeText(txn.id);
        toast.success(`Transaction ID copied: ${txn.id.slice(0, 10)}...`, {
          style: {
            background: '#1A1A2E',
            color: '#FFFFFF',
            border: '1px solid rgba(255,255,255,0.08)'
          }
        });
      }
    }, 600); // 600ms hold trigger
  };

  const endPress = (e) => {
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current);
    }
    
    // Prevent normal click if it was a long press hold
    if (isLongPress.current) {
      e.preventDefault();
      e.stopPropagation();
      return;
    }

    if (onClick && e.type !== 'touchend') {
      onClick();
    }
  };

  const handleTouchEnd = (e) => {
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current);
    }
    if (!isLongPress.current && onClick) {
      onClick();
    }
  };

  return (
    <div
      onMouseDown={startPress}
      onMouseUp={endPress}
      onTouchStart={startPress}
      onTouchEnd={handleTouchEnd}
      onMouseLeave={() => clearTimeout(longPressTimer.current)}
      className="flex items-center justify-between p-3.5 rounded-2xl bg-white/[0.02] hover:bg-white/[0.04] active:bg-white/[0.06] border border-white/[0.04] transition-all cursor-pointer select-none"
    >
      <div className="flex items-center gap-3">
        <Avatar name={txn.contactName || txn.upiId} size="sm" />
        <div className="flex flex-col max-w-[180px]">
          <span className="text-xs font-bold text-white tracking-wide truncate">
            {txn.contactName || txn.upiId}
          </span>
          <span className="text-[10px] text-textSecondary mt-0.5">
            {formatDate(txn.timestamp)}
          </span>
        </div>
      </div>

      <div className="flex flex-col items-end gap-1.5 flex-shrink-0">
        <span className={`text-xs font-bold amount-font tracking-tight ${isCredit ? 'text-success' : 'text-white'}`}>
          {isCredit ? '+' : '-'}{formatAmount(txn.amount)}
        </span>
        <Badge>{txn.status}</Badge>
      </div>
    </div>
  );
};

export default TransactionItem;
