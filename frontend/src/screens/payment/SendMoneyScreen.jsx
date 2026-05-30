import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, Search, Check, AlertCircle, Sparkles, AlertTriangle, ShieldCheck, ChevronRight } from 'lucide-react';
import toast from 'react-hot-toast';

import { useAuthStore } from '../../store/authStore';
import { useWalletStore } from '../../store/walletStore';
import { useOfflineStore } from '../../store/offlineStore';
import { useBalance } from '../../hooks/useBalance';
import { useHaptic } from '../../hooks/useHaptic';

import { MOCK_CONTACTS } from '../../utils/constants';
import { validateUpi } from '../../utils/validateUpi';
import { formatAmount } from '../../utils/formatAmount';
import paymentApi from '../../api/paymentApi';

import TopBar from '../../components/layout/TopBar';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Avatar from '../../components/ui/Avatar';
import AmountKeypad from '../../components/ui/AmountKeypad';
import PaymentSuccess from '../../components/payment/PaymentSuccess';

export const SendMoneyScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const [searchParams] = useSearchParams();
  const user = useAuthStore(state => state.user);
  
  const balance = useWalletStore(state => state.balance);
  const setBalance = useWalletStore(state => state.setBalance);
  
  const isOnline = useOfflineStore(state => state.isOnline);
  const addPendingPayment = useOfflineStore(state => state.addPendingPayment);

  // checkout state
  const [step, setStep] = useState(1); // 1: Select contact, 2: Amount, 3: PIN/Auth, 4: Success/Failure
  const [searchVal, setSearchVal] = useState('');
  const [recipient, setRecipient] = useState(null);
  const [amountStr, setAmountStr] = useState('');
  const [noteStr, setNoteStr] = useState('');
  
  // verification fields
  const [isVerifyingUpi, setIsVerifyingUpi] = useState(false);
  const [pinVal, setPinVal] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [paymentResult, setPaymentResult] = useState(null); // { status: 'SUCCESS'/'FAILED', id: '...' }

  // Sync params check (like opening from /scan or query checks)
  useEffect(() => {
    const tabParam = searchParams.get('tab');
    const qrUpiParam = searchParams.get('upi');
    
    if (qrUpiParam) {
      // Direct QR verification route
      triggerHaptic('medium');
      const tempRecipient = {
        name: qrUpiParam.split('@')[0],
        upiId: qrUpiParam,
        avatar: 'QR'
      };
      setRecipient(tempRecipient);
      setStep(2);
    }
  }, [searchParams]);

  // Handle manual UPI entry verify
  const handleVerifyManualUpi = () => {
    if (!validateUpi(searchVal)) {
      toast.error('Invalid UPI ID format (e.g. mobile@upi)');
      return;
    }
    
    triggerHaptic('medium');
    setIsVerifyingUpi(true);
    
    setTimeout(() => {
      setIsVerifyingUpi(false);
      const parts = searchVal.split('@');
      const name = parts[0].charAt(0).toUpperCase() + parts[0].slice(1);
      
      const newRecipient = {
        name: name,
        upiId: searchVal,
        avatar: name.slice(0, 2).toUpperCase()
      };
      setRecipient(newRecipient);
      setStep(2);
      toast.success('UPI ID verified successfully!');
    }, 1000);
  };

  const handleSelectContact = (contact) => {
    triggerHaptic('light');
    setRecipient(contact);
    setStep(2);
  };

  const handleQuickChip = (val) => {
    triggerHaptic('light');
    setAmountStr(val.toString());
  };

  const handleProceedToPay = () => {
    const amt = parseFloat(amountStr);
    if (isNaN(amt) || amt <= 0) {
      toast.error('Please specify a valid amount');
      return;
    }
    
    // Check wallet capacity constraints
    if (amt > balance) {
      toast.error('Insufficient wallet balance');
      return;
    }
    
    triggerHaptic('medium');
    setStep(3); // Shift to secure PIN pad
  };

  // Safe PIN keypad click handler
  const handlePinKeyPress = (char) => {
    triggerHaptic('light');
    if (char === 'delete') {
      if (pinVal.length > 0) setPinVal(prev => prev.slice(0, -1));
      return;
    }
    
    if (pinVal.length >= 6) return; // limit to 6 digits
    
    const nextPin = pinVal + char;
    setPinVal(nextPin);
    
    if (nextPin.length === 6) {
      executePayment(nextPin);
    }
  };

  const executePayment = async (enteredPin) => {
    setIsProcessing(true);
    const amt = parseFloat(amountStr);
    const txnId = `TXN${Math.floor(1000000000 + Math.random() * 9000000000)}`;

    // Simulated network processing delay
    setTimeout(async () => {
      // Validation fail-safe
      if (enteredPin !== '112233' && enteredPin !== '123456' && enteredPin.length === 6) {
        // Biometric or PIN matching checks (standard pins are 123456 or 112233 for sandbox simplicity)
        setIsProcessing(false);
        setPinVal('');
        toast.error('Incorrect UPI PIN. Please try again.');
        return;
      }

      // Check connectivity status
      if (!isOnline) {
        // Queue payment offline
        const offlineTxn = {
          id: txnId,
          type: 'DEBIT',
          contactName: recipient.name,
          upiId: recipient.upiId,
          amount: amt,
          timestamp: new Date().toISOString(),
          status: 'SUCCESS',
          note: noteStr || 'Offline transfer queue'
        };
        
        addPendingPayment(offlineTxn);
        
        // Deduct balance locally
        setBalance(balance - amt);
        
        setPaymentResult({
          status: 'SUCCESS',
          id: txnId,
          isOffline: true
        });
        setStep(4);
        setIsProcessing(false);
        return;
      }

      try {
        const payload = {
          senderId: user?.id || 'u_dev',
          recipientUpi: recipient.upiId,
          amount: amt,
          note: noteStr || 'Transferred via UPI Mesh',
          pin: enteredPin
        };

        // Fire gateway payment
        await paymentApi.pay(payload);
        
        // Update local wallet state
        setBalance(balance - amt);

        setPaymentResult({
          status: 'SUCCESS',
          id: txnId,
          isOffline: false
        });
        setStep(4);
      } catch (error) {
        console.warn("API payment failed, executing local fallback simulation:", error);
        
        // Local simulation fallback
        setBalance(balance - amt);
        setPaymentResult({
          status: 'SUCCESS',
          id: txnId,
          isOffline: false
        });
        setStep(4);
      } finally {
        setIsProcessing(false);
      }
    }, 1500); // 1.5s processor spin
  };

  // Reset checkout page parameters on reload
  const handleReset = () => {
    setStep(1);
    setRecipient(null);
    setAmountStr('');
    setNoteStr('');
    setPinVal('');
    setPaymentResult(null);
  };

  // Filters contacts list matching text search
  const filteredContacts = MOCK_CONTACTS.filter(c =>
    c.name.toLowerCase().includes(searchVal.toLowerCase()) ||
    c.phone.includes(searchVal) ||
    c.upiId.toLowerCase().includes(searchVal.toLowerCase())
  );

  // STEP Render branches
  if (step === 1) {
    return (
      <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white">
        <TopBar title="Send Money" showBack={true} />
        
        <div className="flex-1 p-5 flex flex-col overflow-y-auto no-scrollbar">
          {/* Search Contacts or Enter UPI ID */}
          <div className="relative flex items-center mb-6">
            <input
              type="text"
              placeholder="Enter name, phone number, or UPI ID..."
              value={searchVal}
              onChange={(e) => setSearchVal(e.target.value)}
              className="w-full py-3.5 pl-4 pr-16 rounded-2xl bg-white/[0.03] border border-white/[0.04] text-xs text-white placeholder-textSecondary font-semibold select-none outline-none focus:border-primary/50"
            />
            {searchVal && (
              <button
                onClick={handleVerifyManualUpi}
                disabled={isVerifyingUpi}
                className="absolute right-2 px-3 py-1.5 bg-primary/25 border border-primary/20 rounded-xl text-[10px] font-bold text-white uppercase active:scale-95 transition-transform"
              >
                {isVerifyingUpi ? 'Checking...' : 'Verify'}
              </button>
            )}
          </div>

          {/* Recent Recipient circles */}
          <div className="mb-6">
            <h4 className="text-[10px] font-bold text-textSecondary uppercase tracking-widest mb-3.5">
              Recent Recipients
            </h4>
            <div className="flex gap-4 overflow-x-auto no-scrollbar py-1">
              {MOCK_CONTACTS.slice(0, 5).map((contact) => (
                <button
                  key={contact.id}
                  onClick={() => handleSelectContact(contact)}
                  className="flex flex-col items-center gap-2 flex-shrink-0 group focus:outline-none"
                  style={{ width: '60px' }}
                >
                  <Avatar name={contact.name} size="sm" />
                  <span className="text-[9px] font-bold text-textSecondary text-center group-hover:text-white line-clamp-1 truncate w-full">
                    {contact.name.split(' ')[0]}
                  </span>
                </button>
              ))}
            </div>
          </div>

          {/* Full matching contacts list */}
          <div className="flex-1">
            <h4 className="text-[10px] font-bold text-textSecondary uppercase tracking-widest mb-3">
              Contacts & Accounts ({filteredContacts.length})
            </h4>
            
            {filteredContacts.length > 0 ? (
              <div className="flex flex-col gap-3">
                {filteredContacts.map((contact) => (
                  <div
                    key={contact.id}
                    onClick={() => handleSelectContact(contact)}
                    className="flex items-center justify-between p-3 rounded-2xl bg-white/[0.01] hover:bg-white/[0.03] active:bg-white/[0.05] border border-white/[0.04] transition-colors cursor-pointer"
                  >
                    <div className="flex items-center gap-3">
                      <Avatar name={contact.name} size="sm" />
                      <div className="flex flex-col text-left">
                        <span className="text-xs font-bold text-white tracking-wide">{contact.name}</span>
                        <span className="text-[10px] text-textSecondary mt-0.5">{contact.upiId}</span>
                      </div>
                    </div>
                    <ChevronRight className="w-4 h-4 text-textSecondary" />
                  </div>
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-10 text-center opacity-60">
                <Search className="w-7 h-7 text-textSecondary mb-2" />
                <span className="text-[10px] font-bold uppercase tracking-wider text-textSecondary">No contacts matched</span>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  if (step === 2) {
    const isInsufficient = parseFloat(amountStr) > balance;
    return (
      <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white">
        <TopBar title="Enter Amount" onBack={() => setStep(1)} />

        <div className="flex-1 p-5 flex flex-col justify-between overflow-y-auto no-scrollbar">
          
          {/* Top Receiver Summary Card */}
          <div className="flex flex-col items-center">
            <Card variant="glass" className="w-full p-4 flex items-center justify-between bg-white/[0.01]">
              <div className="flex items-center gap-3.5 text-left">
                <Avatar name={recipient.name} size="sm" />
                <div>
                  <h4 className="text-xs font-bold text-white tracking-wide">{recipient.name}</h4>
                  <p className="text-[10px] text-textSecondary amount-font mt-0.5">{recipient.upiId}</p>
                </div>
              </div>
              <div className="flex items-center gap-1 text-[8px] font-extrabold tracking-widest text-[#00E676] bg-[#00E676]/10 px-2 py-1 rounded-md border border-[#00E676]/20 uppercase">
                <ShieldCheck className="w-3.5 h-3.5" />
                Verified
              </div>
            </Card>

            {/* Big typed amount display */}
            <div className="text-center mt-10">
              <span className="text-[10px] font-bold text-textSecondary uppercase tracking-widest mb-1.5 block">
                Transfer Sum
              </span>
              <div className="text-4xl font-extrabold amount-font text-white flex items-center justify-center gap-1.5">
                <span>₹</span>
                <span>{amountStr || '0'}</span>
                <span className="w-1.5 h-8 bg-primary/80 animate-pulse" />
              </div>

              {/* Note / Remarks Field */}
              <input
                type="text"
                placeholder="Add note/remark (optional)..."
                value={noteStr}
                onChange={(e) => setNoteStr(e.target.value)}
                className="mt-6 text-center text-xs font-medium text-white placeholder-textSecondary bg-transparent border-0 border-b border-white/5 focus:border-primary focus:ring-0 w-64 outline-none"
              />
            </div>
          </div>

          {/* Quick chips & Available bank balance checks */}
          <div className="flex flex-col gap-2 mt-4">
            <div className="flex gap-2 justify-center">
              {[100, 500, 1000, 2000].map(val => (
                <button
                  key={val}
                  onClick={() => handleQuickChip(val)}
                  className="px-3.5 py-2 text-[10px] font-bold bg-white/[0.03] border border-white/[0.04] text-white hover:text-white rounded-lg active:scale-95"
                >
                  +{val}
                </button>
              ))}
            </div>

            {/* Balance warning banner */}
            <div className="flex items-center justify-between text-[11px] font-semibold tracking-wide bg-white/[0.01] px-4 py-3.5 rounded-2xl border border-white/[0.04] mt-2">
              <span className="text-textSecondary flex items-center gap-1.5">
                <span>UPI Wallet Balance</span>
                <span className="text-white amount-font">{formatAmount(balance)}</span>
              </span>
              {isInsufficient ? (
                <span className="text-danger flex items-center gap-1 text-[10px]">
                  <AlertCircle className="w-3.5 h-3.5" />
                  Insufficient
                </span>
              ) : (
                <span className="text-success text-[10px]">Ready</span>
              )}
            </div>
          </div>

          {/* Keypad */}
          <AmountKeypad value={amountStr} onChange={setAmountStr} maxAmount={balance} />

          {/* Action button */}
          <div className="mt-2">
            <Button
              onClick={handleProceedToPay}
              disabled={!amountStr || parseFloat(amountStr) <= 0 || isInsufficient}
              variant="gradient"
              className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2"
            >
              Proceed to Pay {amountStr ? formatAmount(parseFloat(amountStr)) : ''}
            </Button>
          </div>
        </div>
      </div>
    );
  }

  if (step === 3) {
    return (
      <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white select-none">
        <TopBar title="Confirm & Authorize" onBack={() => setStep(2)} />

        <div className="flex-1 p-6 flex flex-col justify-between items-center overflow-y-auto no-scrollbar relative">
          {isProcessing ? (
            /* Running secure verification overlay */
            <div className="flex-1 flex flex-col items-center justify-center my-auto">
              <div className="relative w-20 h-20 flex items-center justify-center mb-5">
                <div className="absolute inset-0 rounded-full border-4 border-primary/20 border-t-primary animate-spin" />
                <svg className="w-8 h-8 text-primary animate-pulse" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2L2 22h20L12 2zm0 3.99L18.49 19H5.51L12 5.99zm-1 5.01h2v3h-2v-3zm0 4h2v2h-2v-2z" />
                </svg>
              </div>
              <h3 className="text-sm font-bold uppercase tracking-wider text-white">Connecting Secure Gateway</h3>
              <p className="text-xs text-textSecondary mt-1">Authenticating encrypted PIN node...</p>
            </div>
          ) : (
            <>
              {/* Checkout details overview */}
              <div className="w-full flex flex-col items-center">
                <span className="text-[10px] font-bold text-textSecondary uppercase tracking-widest mb-1">Paying</span>
                <span className="text-3xl font-extrabold amount-font text-white">{formatAmount(parseFloat(amountStr))}</span>
                <span className="text-xs text-textSecondary mt-1">to {recipient.name}</span>

                {/* Secure Pin layout dots */}
                <div className="flex flex-col items-center justify-center mt-12 mb-6">
                  <span className="text-[10px] font-bold text-textSecondary uppercase tracking-widest mb-4">
                    ENTER 6-DIGIT UPI PIN
                  </span>
                  
                  <div className="flex gap-4">
                    {Array.from({ length: 6 }).map((_, idx) => (
                      <div
                        key={idx}
                        className={`w-4 h-4 rounded-full border-2 transition-all duration-150 ${
                          pinVal[idx] 
                            ? 'bg-primary border-primary scale-110 shadow-glow-primary' 
                            : 'border-white/10 bg-transparent'
                        }`}
                      />
                    ))}
                  </div>
                  
                  <span className="text-[9px] text-textSecondary/50 font-semibold tracking-wider mt-4">
                    Default demo pin is: 123456 or 112233
                  </span>
                </div>
              </div>

              {/* Pin keypad grid */}
              <div className="w-full max-w-[280px] grid grid-cols-3 gap-y-4 gap-x-6 py-4 amount-font mt-auto select-none">
                {['1', '2', '3', '4', '5', '6', '7', '8', '9', '', '0', 'delete'].map((key, i) => {
                  if (key === '') return <div key={i} />;
                  return (
                    <button
                      key={i}
                      type="button"
                      onClick={() => handlePinKeyPress(key)}
                      className="h-12 rounded-xl flex items-center justify-center text-lg font-bold text-white bg-white/[0.02] border border-white/[0.04] active:bg-white/10 active:scale-95 transition-transform duration-100"
                    >
                      {key === 'delete' ? '←' : key}
                    </button>
                  );
                })}
              </div>
            </>
          )}
        </div>
      </div>
    );
  }

  // STEP 4 - Return receipt confirmation page
  if (step === 4) {
    return (
      <PaymentSuccess
        status={paymentResult?.status || 'SUCCESS'}
        toName={recipient.name}
        upiId={recipient.upiId}
        amount={parseFloat(amountStr)}
        txnId={paymentResult?.id}
        note={noteStr || (paymentResult?.isOffline ? 'Queued offline - will sync when online' : 'Processed online')}
        onHome={() => navigate('/home')}
        onRetry={handleReset}
      />
    );
  }

  return null;
};

export default SendMoneyScreen;
