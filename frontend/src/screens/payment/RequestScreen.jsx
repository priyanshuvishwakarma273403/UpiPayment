import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { MessageSquare, Share2, Copy, ArrowLeft, ArrowUpRight, Sparkles, Check, Users } from 'lucide-react';
import toast from 'react-hot-toast';

import { useAuthStore } from '../../store/authStore';
import { useHaptic } from '../../hooks/useHaptic';
import { MOCK_CONTACTS } from '../../utils/constants';
import { formatAmount } from '../../utils/formatAmount';
import { validateUpi } from '../../utils/validateUpi';

import TopBar from '../../components/layout/TopBar';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Avatar from '../../components/ui/Avatar';
import AmountKeypad from '../../components/ui/AmountKeypad';
import BottomSheet from '../../components/ui/BottomSheet';

export const RequestScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const user = useAuthStore(state => state.user);

  // States
  const [recipient, setRecipient] = useState(null);
  const [amountStr, setAmountStr] = useState('');
  const [noteStr, setNoteStr] = useState('');
  
  // Results
  const [isGenerated, setIsGenerated] = useState(false);
  const [generatedUri, setGeneratedUri] = useState('');
  
  // Drawers
  const [isContactSheetOpen, setIsContactSheetOpen] = useState(false);
  const [manualUpi, setManualUpi] = useState('');

  const handleSelectContact = (contact) => {
    triggerHaptic('light');
    setRecipient(contact);
    setIsContactSheetOpen(false);
  };

  const handleManualUpiSubmit = () => {
    if (!validateUpi(manualUpi)) {
      toast.error('Invalid UPI ID format');
      return;
    }
    triggerHaptic('light');
    const parts = manualUpi.split('@');
    const name = parts[0].charAt(0).toUpperCase() + parts[0].slice(1);
    setRecipient({
      name: name,
      upiId: manualUpi,
      avatar: name.slice(0, 2).toUpperCase()
    });
    setIsContactSheetOpen(false);
  };

  const handleGenerate = () => {
    const amt = parseFloat(amountStr);
    if (!recipient) {
      toast.error('Please specify a receiver/contact');
      return;
    }
    if (isNaN(amt) || amt <= 0) {
      toast.error('Please specify a valid amount');
      return;
    }

    triggerHaptic('medium');
    
    // Construct standard UPI URI protocol: upi://pay?pa=UPIID&pn=NAME&am=AMOUNT&tn=NOTE
    const uri = `upi://pay?pa=${user?.upiId || 'myupi@upimesh'}&pn=${encodeURIComponent(user?.name || 'User')}&am=${amt}&tn=${encodeURIComponent(noteStr || 'Request from UPI Mesh')}`;
    
    setGeneratedUri(uri);
    setIsGenerated(true);
    toast.success('Request QR generated successfully!');
  };

  const handleShareSocial = (platform) => {
    triggerHaptic('light');
    const shareText = `UPI Mesh Request: Please pay ₹${amountStr} to ${user?.name} via link: ${generatedUri}`;
    
    if (platform === 'whatsapp') {
      window.open(`https://api.whatsapp.com/send?text=${encodeURIComponent(shareText)}`, '_blank');
    } else if (platform === 'sms') {
      window.open(`sms:?body=${encodeURIComponent(shareText)}`, '_blank');
    } else {
      // General clipboard copy
      navigator.clipboard.writeText(generatedUri);
      toast.success('Request link copied to clipboard!');
    }
  };

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white select-none">
      <TopBar title="Request Money" />

      <div className="flex-1 p-5 overflow-y-auto no-scrollbar flex flex-col justify-between">
        {!isGenerated ? (
          /* SETUP REQUEST */
          <div className="flex-1 flex flex-col justify-between">
            <div className="flex flex-col gap-5">
              
              {/* Recipient select box */}
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-semibold tracking-wider text-textSecondary uppercase pl-1">
                  Request From
                </label>
                
                {recipient ? (
                  <Card
                    variant="glass"
                    onClick={() => { triggerHaptic('light'); setIsContactSheetOpen(true); }}
                    className="p-4 flex items-center justify-between bg-white/[0.01]"
                    animate={false}
                  >
                    <div className="flex items-center gap-3">
                      <Avatar name={recipient.name} size="sm" />
                      <div className="flex flex-col text-left">
                        <span className="text-xs font-bold text-white tracking-wide">{recipient.name}</span>
                        <span className="text-[10px] text-textSecondary mt-0.5">{recipient.upiId}</span>
                      </div>
                    </div>
                    <span className="text-[10px] text-secondary font-bold uppercase bg-white/5 px-2.5 py-1 rounded-lg">Change</span>
                  </Card>
                ) : (
                  <button
                    type="button"
                    onClick={() => { triggerHaptic('light'); setIsContactSheetOpen(true); }}
                    className="w-full py-5 rounded-2xl bg-white/[0.02] border border-dashed border-white/10 hover:border-primary/50 text-xs font-bold text-textSecondary flex items-center justify-center gap-2 transition-colors"
                  >
                    <Users className="w-4 h-4" />
                    Select Contact / Enter UPI ID
                  </button>
                )}
              </div>

              {/* Amount numeric keyboard input */}
              <div className="flex flex-col items-center justify-center py-4">
                <span className="text-[10px] font-bold text-textSecondary uppercase tracking-widest mb-1.5">Request Sum</span>
                <div className="text-3xl font-extrabold amount-font text-white flex items-center justify-center gap-1">
                  <span>₹</span>
                  <span>{amountStr || '0'}</span>
                  <span className="w-1.5 h-7 bg-primary/80 animate-pulse" />
                </div>

                <input
                  type="text"
                  placeholder="Request description note..."
                  value={noteStr}
                  onChange={(e) => setNoteStr(e.target.value)}
                  className="mt-5 text-center text-xs font-medium text-white placeholder-textSecondary bg-transparent border-0 border-b border-white/5 focus:border-primary focus:ring-0 w-64 outline-none"
                />
              </div>
            </div>

            {/* Custom keypad */}
            <AmountKeypad value={amountStr} onChange={setAmountStr} maxAmount={10000} />

            {/* Button */}
            <div className="mt-2">
              <Button
                onClick={handleGenerate}
                disabled={!recipient || !amountStr || parseFloat(amountStr) <= 0}
                variant="gradient"
                className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2"
              >
                <ArrowUpRight className="w-4 h-4 rotate-180" />
                Generate Request Details
              </Button>
            </div>
          </div>
        ) : (
          /* RENDER REQUEST QR */
          <div className="flex-1 flex flex-col justify-between items-center py-6">
            <div className="w-full flex flex-col items-center">
              
              {/* Target info card */}
              <span className="text-[10px] text-textSecondary font-bold uppercase tracking-widest mb-1">Requesting</span>
              <span className="text-3xl font-black text-white amount-font">{formatAmount(parseFloat(amountStr))}</span>
              <span className="text-xs text-textSecondary mt-1">from {recipient.name}</span>

              {/* QR frame */}
              <div className="mt-8 p-6 bg-white rounded-[32px] shadow-glow-primary border border-white/20 relative flex items-center justify-center">
                <QRCodeSVG
                  value={generatedUri}
                  size={180}
                  level="H"
                  includeMargin={false}
                  imageSettings={{
                    src: '/vite.svg',
                    x: undefined,
                    y: undefined,
                    height: 24,
                    width: 24,
                    excavate: true,
                  }}
                />
                
                {/* Center visual branding dot */}
                <div className="absolute w-8 h-8 rounded-xl bg-slate-900 border-2 border-white flex items-center justify-center">
                  <svg className="w-4.5 h-4.5 text-primary fill-current" viewBox="0 0 24 24">
                    <path d="M12 2L2 22h20L12 2zm0 3.99L18.49 19H5.51L12 5.99z" />
                  </svg>
                </div>
              </div>

              <span className="text-[10px] text-textSecondary font-bold tracking-wider uppercase mt-4">
                Scan to pay {user?.name}
              </span>
            </div>

            {/* Sharing layouts */}
            <div className="w-full flex flex-col gap-3.5 mt-8">
              <div className="flex items-center gap-2 w-full">
                <button
                  onClick={() => handleShareSocial('whatsapp')}
                  className="flex-1 py-3.5 bg-emerald-500/10 active:bg-emerald-500/20 text-emerald-400 border border-emerald-500/20 text-xs font-bold rounded-2xl flex items-center justify-center gap-2 transition-transform duration-100 active:scale-95"
                >
                  {/* WhatsApp simple vector */}
                  <svg className="w-4 h-4 fill-current" viewBox="0 0 24 24">
                    <path d="M12.012 2c-5.506 0-9.988 4.482-9.988 9.988 0 1.76.459 3.474 1.33 4.988L2 22l5.139-1.348c1.472.802 3.129 1.224 4.819 1.225 5.505 0 9.988-4.481 9.988-9.988S17.518 2 12.012 2zm6.002 13.998c-.247.697-1.218 1.265-1.685 1.32-.467.056-.913.262-2.984-.572-2.493-1.004-4.095-3.543-4.22-3.71-.125-.167-1.01-1.343-1.01-2.56 0-1.218.636-1.815.862-2.06.226-.245.49-.307.653-.307.164 0 .327.001.469.008.148.007.348-.056.544.417.202.489.691 1.688.75 1.808.058.12.098.26.019.418-.08.158-.12.257-.238.397-.118.14-.249.31-.356.417-.118.12-.243.25-.104.49.139.24.615 1.017 1.319 1.643.903.803 1.662 1.05 1.898 1.168.236.118.375.099.514-.06.139-.16.597-.697.756-.935.16-.24.318-.2.535-.12.217.08 1.378.65 1.616.77.239.12.397.18.457.28.06.1.06.58-.187 1.277z" />
                  </svg>
                  WhatsApp
                </button>
                <button
                  onClick={() => handleShareSocial('sms')}
                  className="flex-1 py-3.5 bg-blue-500/10 active:bg-blue-500/20 text-blue-400 border border-blue-500/20 text-xs font-bold rounded-2xl flex items-center justify-center gap-2 transition-transform duration-100 active:scale-95"
                >
                  <MessageSquare className="w-4 h-4" />
                  SMS
                </button>
              </div>

              <div className="flex gap-2 w-full">
                <button
                  onClick={() => handleShareSocial('copy')}
                  className="flex-1 py-3.5 bg-white/5 active:bg-white/10 rounded-2xl text-xs font-bold text-white border border-white/10 flex items-center justify-center gap-2 transition-transform duration-100 active:scale-95"
                >
                  <Copy className="w-4 h-4 text-textSecondary" />
                  Copy Link
                </button>
                <Button onClick={handleReset} variant="outline" className="flex-1 py-3.5 text-xs font-bold">
                  New Request
                </Button>
              </div>

              <Button onClick={() => navigate('/home')} variant="gradient" className="w-full py-4 font-bold text-sm">
                Back to Home
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* Select contact sheet drawer */}
      <BottomSheet isOpen={isContactSheetOpen} onClose={() => setIsContactSheetOpen(false)} title="Select Request Contact">
        <div className="flex flex-col gap-4 my-2">
          {/* Manual input */}
          <div className="flex gap-2 items-end">
            <Input
              label="Request manually"
              placeholder="e.g. mobile@upi"
              value={manualUpi}
              onChange={(e) => setManualUpi(e.target.value)}
              containerClassName="flex-1"
            />
            <Button onClick={handleManualUpiSubmit} disabled={!manualUpi} variant="glass" className="py-3.5 rounded-2xl">Verify</Button>
          </div>

          <div className="h-px bg-white/5 my-2" />

          {/* Quick Contacts lists */}
          <div className="flex flex-col gap-3 max-h-[300px] overflow-y-auto no-scrollbar">
            {MOCK_CONTACTS.map((contact) => (
              <div
                key={contact.id}
                onClick={() => handleSelectContact(contact)}
                className="p-3 rounded-2xl flex items-center gap-3 bg-white/[0.01] hover:bg-white/[0.03] cursor-pointer border border-white/[0.04]"
              >
                <Avatar name={contact.name} size="sm" />
                <div className="text-left">
                  <h4 className="text-xs font-bold text-white tracking-wide">{contact.name}</h4>
                  <p className="text-[10px] text-textSecondary mt-0.5">{contact.upiId}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </BottomSheet>
    </div>
  );
};

export default RequestScreen;
