import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { Store, Download, Share2, Plus, RefreshCw, Clock, ArrowLeft, Landmark, Check } from 'lucide-react';
import toast from 'react-hot-toast';

import { useAuthStore } from '../../store/authStore';
import { useHaptic } from '../../hooks/useHaptic';
import instance from '../../api/axios';
import { formatAmount } from '../../utils/formatAmount';
import { formatDate } from '../../utils/formatDate';

import TopBar from '../../components/layout/TopBar';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import BottomSheet from '../../components/ui/BottomSheet';

export const MerchantScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const user = useAuthStore(state => state.user);

  // States
  const [collectionToday, setCollectionToday] = useState(12840.00);
  const [staticUri, setStaticUri] = useState('');
  const [collectionsList, setCollectionsList] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // Dynamic QR States
  const [isDynamicSheetOpen, setIsDynamicSheetOpen] = useState(false);
  const [dynamicAmount, setDynamicAmount] = useState('');
  const [dynamicDesc, setDynamicDesc] = useState('');
  const [dynamicQrUri, setDynamicQrUri] = useState('');
  const [isDynamicGenerated, setIsDynamicGenerated] = useState(false);
  const [timerLeft, setTimerLeft] = useState(900); // 15 minutes in seconds

  // Initialize merchant data
  useEffect(() => {
    const upiId = user?.upiId || 'shop@upimesh';
    // Static QR points to standard payments link
    setStaticUri(`upi://pay?pa=${upiId}&pn=${encodeURIComponent(user?.name || 'Shopkeeper')}`);

    // Mock Collections ledger
    setCollectionsList([
      { id: 'm_tx_01', senderName: 'Rohit Sharma', amount: 350.00, timestamp: new Date(Date.now() - 1000 * 60 * 12).toISOString(), status: 'SUCCESS' },
      { id: 'm_tx_02', senderName: 'Ananya Roy', amount: 1500.00, timestamp: new Date(Date.now() - 1000 * 60 * 45).toISOString(), status: 'SUCCESS' },
      { id: 'm_tx_03', senderName: 'Sanjay Dutt', amount: 45.00, timestamp: new Date(Date.now() - 1000 * 60 * 120).toISOString(), status: 'SUCCESS' },
      { id: 'm_tx_04', senderName: 'Sneha Rao', amount: 120.00, timestamp: new Date(Date.now() - 1000 * 60 * 300).toISOString(), status: 'SUCCESS' },
    ]);
  }, [user]);

  // Dynamic QR Expiration Countdown timer
  useEffect(() => {
    if (!isDynamicGenerated || timerLeft <= 0) return;

    const timer = setInterval(() => {
      setTimerLeft(prev => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [isDynamicGenerated, timerLeft]);

  const handleGenerateDynamic = () => {
    const amt = parseFloat(dynamicAmount);
    if (!dynamicAmount || isNaN(amt) || amt <= 0) {
      toast.error('Please enter a valid amount');
      return;
    }

    triggerHaptic('medium');
    const dynamicUriVal = `upi://pay?pa=${user?.upiId || 'shop@upimesh'}&pn=${encodeURIComponent(user?.name || 'Shop')}&am=${amt}&tn=${encodeURIComponent(dynamicDesc || 'Invoice payment')}`;
    
    setDynamicQrUri(dynamicUriVal);
    setIsDynamicGenerated(true);
    setTimerLeft(900); // Reset countdown to 15m
    setIsDynamicSheetOpen(false);
    toast.success('Dynamic billing QR generated!');
  };

  const handleDownloadQr = (type = 'static') => {
    triggerHaptic('light');
    toast.success(`Downloading ${type} QR Code image file...`);
  };

  const handleShareQr = () => {
    triggerHaptic('light');
    toast.success('Static store QR link copied for sharing!');
  };

  const formatCountdown = (secs) => {
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white select-none">
      {/* Top Header */}
      <TopBar title="Merchant Dashboard" />

      <div className="flex-1 p-5 overflow-y-auto no-scrollbar flex flex-col gap-6">
        
        {/* Earnings Card */}
        <Card variant="wallet" shine className="p-5 flex flex-col gap-1 text-left relative">
          <div className="absolute right-4 top-4 p-2 bg-white/10 rounded-xl">
            <Store className="w-5 h-5 text-secondary" />
          </div>
          <span className="text-[10px] font-bold text-white/50 tracking-widest uppercase">
            Today's Collections
          </span>
          <span className="text-3xl font-black amount-font text-white">
            {formatAmount(collectionToday)}
          </span>
          <span className="text-[10px] text-success font-semibold mt-1">
            +12.5% increase from yesterday
          </span>
        </Card>

        {/* Generated dynamic billing view */}
        {isDynamicGenerated && timerLeft > 0 ? (
          <Card variant="glass" className="p-5 flex flex-col items-center border border-[#00D2FF]/20 relative">
            <div className="flex justify-between items-center w-full mb-3">
              <span className="text-[10px] text-secondary font-bold uppercase tracking-wider">Dynamic Invoice</span>
              <div className="flex items-center gap-1.5 text-[10px] text-warning font-semibold bg-warning/10 px-2.5 py-1 rounded-lg border border-warning/20">
                <Clock className="w-3.5 h-3.5" />
                <span>Expires in {formatCountdown(timerLeft)}</span>
              </div>
            </div>

            <div className="bg-white p-4 rounded-2xl relative flex items-center justify-center">
              <QRCodeSVG value={dynamicQrUri} size={150} level="M" />
              <div className="absolute w-7 h-7 rounded-lg bg-slate-900 border-2 border-white flex items-center justify-center">
                <svg className="w-3.5 h-3.5 text-secondary fill-current" viewBox="0 0 24 24">
                  <path d="M12 2L2 22h20L12 2zm0 3.99L18.49 19H5.51L12 5.99z" />
                </svg>
              </div>
            </div>

            <span className="text-base font-extrabold amount-font mt-3.5">
              {formatAmount(parseFloat(dynamicAmount))}
            </span>
            {dynamicDesc && <span className="text-[10px] text-textSecondary mt-0.5">{dynamicDesc}</span>}

            <div className="w-full h-px bg-white/5 my-4" />
            <Button
              onClick={() => { triggerHaptic('light'); setIsDynamicGenerated(false); }}
              variant="outline"
              className="py-2.5 text-xs w-full font-bold"
            >
              Clear Invoice
            </Button>
          </Card>
        ) : (
          /* Static Shop QR code view */
          <Card variant="glass" className="p-5 flex flex-col items-center">
            <h4 className="text-xs font-bold text-textSecondary uppercase tracking-widest mb-4">
              My Static QR Code
            </h4>
            
            <div className="bg-white p-4.5 rounded-[28px] relative flex items-center justify-center">
              {staticUri && <QRCodeSVG value={staticUri} size={150} level="M" />}
              <div className="absolute w-7 h-7 rounded-lg bg-slate-900 border-2 border-white flex items-center justify-center">
                <svg className="w-3.5 h-3.5 text-primary fill-current" viewBox="0 0 24 24">
                  <path d="M12 2L2 22h20L12 2zm0 3.99L18.49 19H5.51L12 5.99z" />
                </svg>
              </div>
            </div>

            <span className="text-xs font-bold text-white mt-4">{user?.name || 'Shopkeeper'}</span>
            <span className="text-[10px] text-textSecondary amount-font mt-0.5">{user?.upiId || 'shop@upimesh'}</span>

            <div className="flex gap-2.5 w-full mt-5">
              <button
                onClick={() => handleDownloadQr('static')}
                className="flex-1 py-3 bg-white/5 active:bg-white/10 rounded-2xl text-xs font-bold text-white border border-white/10 flex items-center justify-center gap-1.5"
              >
                <Download className="w-4 h-4 text-textSecondary" />
                Download
              </button>
              <button
                onClick={handleShareQr}
                className="flex-1 py-3 bg-white/5 active:bg-white/10 rounded-2xl text-xs font-bold text-white border border-white/10 flex items-center justify-center gap-1.5"
              >
                <Share2 className="w-4 h-4 text-textSecondary" />
                Share QR
              </button>
            </div>
          </Card>
        )}

        {/* Generate Dynamic Action Trigger */}
        <Button
          onClick={() => { triggerHaptic('light'); setIsDynamicSheetOpen(true); setIsDynamicGenerated(false); setDynamicAmount(''); setDynamicDesc(''); }}
          variant="gradient"
          className="py-4 font-bold text-sm w-full flex items-center justify-center gap-2"
        >
          <Plus className="w-4 h-4" />
          Generate Dynamic Invoice QR
        </Button>

        {/* Recent Collections Ledger */}
        <div className="flex flex-col gap-3">
          <h4 className="text-[10px] font-bold text-textSecondary uppercase tracking-widest text-left">
            Recent Collections Ledger
          </h4>

          {collectionsList.map((item) => (
            <div
              key={item.id}
              className="p-3.5 rounded-2xl bg-white/[0.01] border border-white/[0.04] flex justify-between items-center"
            >
              <div className="flex items-center gap-3 text-left">
                <div className="w-9 h-9 rounded-xl bg-success/15 border border-success/20 flex items-center justify-center text-success">
                  🪙
                </div>
                <div>
                  <h5 className="text-xs font-bold text-white tracking-wide">{item.senderName}</h5>
                  <p className="text-[9px] text-textSecondary mt-0.5">{formatDate(item.timestamp)}</p>
                </div>
              </div>
              <div className="flex flex-col items-end gap-1">
                <span className="text-xs font-extrabold text-success amount-font">
                  +{formatAmount(item.amount)}
                </span>
                <span className="text-[8px] font-bold uppercase tracking-wider text-success">Success</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Dynamic QR Generator Form Bottom Sheet */}
      <BottomSheet isOpen={isDynamicSheetOpen} onClose={() => setIsDynamicSheetOpen(false)} title="Generate Dynamic Invoice">
        <div className="flex flex-col gap-4 my-2">
          <Input
            label="Amount (₹)"
            type="number"
            placeholder="0.00"
            value={dynamicAmount}
            onChange={(e) => setDynamicAmount(e.target.value)}
          />
          <Input
            label="Description Note"
            placeholder="e.g. Table 4 Dinner Split"
            value={dynamicDesc}
            onChange={(e) => setDynamicDesc(e.target.value)}
          />
          <Button
            onClick={handleGenerateDynamic}
            disabled={!dynamicAmount}
            variant="gradient"
            className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2"
          >
            <Check className="w-4 h-4" />
            Generate Dynamic QR
          </Button>
        </div>
      </BottomSheet>
    </div>
  );
};

export default MerchantScreen;
