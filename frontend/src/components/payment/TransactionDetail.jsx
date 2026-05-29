import React from 'react';
import { Copy, Download, AlertTriangle, CheckCircle, XCircle, Info } from 'lucide-react';
import toast from 'react-hot-toast';
import { useHaptic } from '../../hooks/useHaptic';
import BottomSheet from '../ui/BottomSheet';
import Badge from '../ui/Badge';
import { formatAmount } from '../../utils/formatAmount';
import { formatDate } from '../../utils/formatDate';

export const TransactionDetail = ({ txn, isOpen, onClose }) => {
  const triggerHaptic = useHaptic();

  if (!txn) return null;

  const isCredit = txn.type === 'CREDIT';

  const handleCopyId = () => {
    triggerHaptic('light');
    navigator.clipboard.writeText(txn.id);
    toast.success('Transaction ID copied!');
  };

  const handleDownloadReceipt = () => {
    triggerHaptic('light');
    toast.success('Downloading receipt (PDF preview)...');
  };

  const handleReportIssue = () => {
    triggerHaptic('medium');
    toast('Dispute support opened. Connecting to support team...', { icon: '🤝' });
  };

  return (
    <BottomSheet isOpen={isOpen} onClose={onClose} title="Transaction Details">
      <div className="flex flex-col items-center justify-center py-4 select-none">
        
        {/* Large Amount */}
        <span className={`text-3xl font-extrabold tracking-tight amount-font ${isCredit ? 'text-success' : 'text-white'}`}>
          {isCredit ? '+' : '-'}{formatAmount(txn.amount)}
        </span>
        
        {/* Status Badge */}
        <div className="mt-2">
          <Badge>{txn.status}</Badge>
        </div>

        {/* Divider */}
        <div className="w-full h-px bg-white/5 my-5" />

        {/* Details Grid */}
        <div className="w-full flex flex-col gap-3.5 px-1">
          <div className="flex justify-between items-center text-xs">
            <span className="text-textSecondary">Transaction ID</span>
            <div className="flex items-center gap-1.5 cursor-pointer hover:text-white" onClick={handleCopyId}>
              <span className="font-semibold text-white amount-font">{txn.id}</span>
              <Copy className="w-3.5 h-3.5 text-textSecondary" />
            </div>
          </div>

          <div className="flex justify-between items-center text-xs">
            <span className="text-textSecondary">Date & Time</span>
            <span className="font-semibold text-white">{formatDate(txn.timestamp)}</span>
          </div>

          <div className="flex justify-between items-center text-xs">
            <span className="text-textSecondary">Payment Type</span>
            <span className="font-semibold text-white">{txn.type === 'CREDIT' ? 'Credited' : 'Debited'}</span>
          </div>

          <div className="flex justify-between items-center text-xs">
            <span className="text-textSecondary">{isCredit ? 'Sender UPI ID' : 'Receiver UPI ID'}</span>
            <span className="font-semibold text-white amount-font">{txn.upiId}</span>
          </div>

          {txn.contactName && (
            <div className="flex justify-between items-center text-xs">
              <span className="text-textSecondary">{isCredit ? 'From' : 'To'}</span>
              <span className="font-semibold text-white">{txn.contactName}</span>
            </div>
          )}

          {txn.note && (
            <div className="flex justify-between items-start text-xs">
              <span className="text-textSecondary">Note</span>
              <span className="font-semibold text-white text-right max-w-[200px] break-all">{txn.note}</span>
            </div>
          )}

          <div className="flex justify-between items-center text-xs">
            <span className="text-textSecondary">Payment Mode</span>
            <span className="font-semibold text-white">UPI (Wallet Account)</span>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="w-full mt-6 flex gap-2">
          <button
            onClick={handleDownloadReceipt}
            className="flex-1 py-3 bg-white/5 active:bg-white/10 rounded-2xl text-xs font-bold text-white flex items-center justify-center gap-1.5 transition-transform duration-100 active:scale-[0.97] border border-white/[0.04]"
          >
            <Download className="w-4 h-4 text-textSecondary" />
            Receipt
          </button>
          
          <button
            onClick={handleReportIssue}
            className="flex-1 py-3 bg-red-500/10 active:bg-red-500/20 rounded-2xl text-xs font-bold text-danger flex items-center justify-center gap-1.5 transition-transform duration-100 active:scale-[0.97] border border-danger/10"
          >
            <AlertTriangle className="w-4 h-4" />
            Report Issue
          </button>
        </div>
      </div>
    </BottomSheet>
  );
};

export default TransactionDetail;
