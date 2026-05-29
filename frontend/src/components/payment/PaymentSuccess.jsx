import React from 'react';
import { motion } from 'framer-motion';
import { Check, X, Share2, Download, Home, RotateCcw } from 'lucide-react';
import toast from 'react-hot-toast';
import { useHaptic } from '../../hooks/useHaptic';
import { formatAmount } from '../../utils/formatAmount';
import Button from '../ui/Button';
import Card from '../ui/Card';

export const PaymentSuccess = ({
  status = 'SUCCESS', // 'SUCCESS' or 'FAILED'
  toName = 'Contact',
  upiId = 'contact@upimesh',
  amount = 0,
  txnId = '',
  note = '',
  onHome,
  onRetry
}) => {
  const triggerHaptic = useHaptic();
  const isSuccess = status === 'SUCCESS';

  const handleShare = () => {
    triggerHaptic('light');
    if (navigator.share) {
      navigator.share({
        title: 'Payment Receipt',
        text: `Successfully paid ${formatAmount(amount)} to ${toName}`,
        url: window.location.href,
      }).catch(err => console.log(err));
    } else {
      toast.success('Receipt details copied to share!');
    }
  };

  const handleDownload = () => {
    triggerHaptic('light');
    toast.success('Downloading PDF receipt...');
  };

  // SVG Drawing animations for checkmark and X mark
  const drawIconPath = {
    hidden: { pathLength: 0, opacity: 0 },
    visible: {
      pathLength: 1,
      opacity: 1,
      transition: { pathLength: { type: 'spring', duration: 1.2, bounce: 0 }, opacity: { duration: 0.1 } }
    }
  };

  return (
    <div className="flex-1 flex flex-col justify-between p-6 select-none bg-[#0A0A0F] h-full">
      <div className="flex-1 flex flex-col items-center justify-center my-auto">
        {/* Success / Failure Icon Animation */}
        <div className="relative flex items-center justify-center w-24 h-24 mb-6">
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: 'spring', stiffness: 200, damping: 15, delay: 0.1 }}
            className={`absolute inset-0 rounded-full bg-gradient-to-tr ${
              isSuccess ? 'from-[#00E676] to-[#00B248]' : 'from-[#FF4757] to-[#FF6B81]'
            } shadow-[0_0_30px_rgba(0,230,118,0.3)] opacity-20`}
          />
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: 'spring', stiffness: 260, damping: 20 }}
            className={`w-20 h-20 rounded-full flex items-center justify-center bg-gradient-to-tr ${
              isSuccess ? 'from-[#00E676] to-[#00B248]' : 'from-[#FF4757] to-[#FF6B81]'
            } shadow-lg`}
          >
            {isSuccess ? (
              <svg className="w-10 h-10 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                <motion.path
                  variants={drawIconPath}
                  initial="hidden"
                  animate="visible"
                  d="M5 13l4 4L19 7"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            ) : (
              <svg className="w-10 h-10 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                <motion.path
                  variants={drawIconPath}
                  initial="hidden"
                  animate="visible"
                  d="M18 6L6 18M6 6l12 12"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            )}
          </motion.div>
        </div>

        {/* Dynamic Headings */}
        <motion.h2
          initial={{ y: 10, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.2 }}
          className="text-xl font-black text-center text-white tracking-wide uppercase"
        >
          {isSuccess ? 'Payment Successful!' : 'Payment Failed'}
        </motion.h2>

        <motion.p
          initial={{ y: 10, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.3 }}
          className="text-xs text-textSecondary text-center mt-1"
        >
          {isSuccess ? 'Your transaction has been processed' : 'Please check your connection or details'}
        </motion.p>

        {/* Receipt details card */}
        <Card variant="glass" className="w-full mt-8 p-5 flex flex-col gap-4 text-xs">
          <div className="flex justify-between items-center pb-3 border-b border-white/[0.04]">
            <span className="text-textSecondary">Paid To</span>
            <div className="text-right">
              <div className="font-bold text-white text-sm">{toName}</div>
              <div className="text-[10px] text-textSecondary amount-font mt-0.5">{upiId}</div>
            </div>
          </div>

          <div className="flex justify-between items-center py-1">
            <span className="text-textSecondary">Amount</span>
            <span className="text-sm font-extrabold text-white amount-font">
              {formatAmount(amount)}
            </span>
          </div>

          {note && (
            <div className="flex justify-between items-center py-1">
              <span className="text-textSecondary">Note</span>
              <span className="font-medium text-white">{note}</span>
            </div>
          )}

          {txnId && (
            <div className="flex justify-between items-center pt-3 border-t border-white/[0.04]">
              <span className="text-textSecondary">Transaction ID</span>
              <span className="font-semibold text-white amount-font text-[10px]">{txnId}</span>
            </div>
          )}
        </Card>
      </div>

      {/* Primary Navigation Buttons */}
      <div className="mt-8 flex flex-col gap-3">
        {isSuccess ? (
          <>
            <div className="flex gap-2 w-full">
              <button
                type="button"
                onClick={handleShare}
                className="flex-1 py-3.5 bg-white/5 active:bg-white/10 rounded-2xl text-xs font-bold text-white flex items-center justify-center gap-2 border border-white/10 transition-transform duration-100 active:scale-[0.97]"
              >
                <Share2 className="w-4 h-4 text-textSecondary" />
                Share
              </button>
              <button
                type="button"
                onClick={handleDownload}
                className="flex-1 py-3.5 bg-white/5 active:bg-white/10 rounded-2xl text-xs font-bold text-white flex items-center justify-center gap-2 border border-white/10 transition-transform duration-100 active:scale-[0.97]"
              >
                <Download className="w-4 h-4 text-textSecondary" />
                Download
              </button>
            </div>
            
            <Button variant="gradient" onClick={onHome} className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2">
              <Home className="w-4 h-4" />
              Back to Home
            </Button>
          </>
        ) : (
          <>
            {onRetry && (
              <Button variant="gradient" onClick={onRetry} className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2">
                <RotateCcw className="w-4 h-4" />
                Retry Payment
              </Button>
            )}
            <Button variant="outline" onClick={onHome} className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2">
              <Home className="w-4 h-4" />
              Go to Home
            </Button>
          </>
        )}
      </div>
    </div>
  );
};

export default PaymentSuccess;
