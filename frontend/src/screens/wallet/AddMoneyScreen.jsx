import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Landmark, ArrowLeft, ArrowUpRight, HelpCircle, Check } from 'lucide-react';
import toast from 'react-hot-toast';

import { useAuthStore } from '../../store/authStore';
import { useWalletStore } from '../../store/walletStore';
import { useBalance } from '../../hooks/useBalance';
import { useHaptic } from '../../hooks/useHaptic';

import TopBar from '../../components/layout/TopBar';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import AmountKeypad from '../../components/ui/AmountKeypad';
import BottomSheet from '../../components/ui/BottomSheet';
import { formatAmount } from '../../utils/formatAmount';
import { MOCK_BANKS } from '../../utils/constants';

export const AddMoneyScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  const user = useAuthStore(state => state.user);
  
  // Custom API coordination hook
  const { addMoney, isAddingMoney } = useBalance(user?.id);
  const localAddMoney = useWalletStore(state => state.addMoney);

  const [amountStr, setAmountStr] = useState('');
  const [selectedBank, setSelectedBank] = useState(MOCK_BANKS[0]);
  const [isBankSheetOpen, setIsBankSheetOpen] = useState(false);

  const handleQuickChip = (val) => {
    triggerHaptic('light');
    setAmountStr(String(val));
  };

  const handleDeposit = async () => {
    const amountVal = parseFloat(amountStr);
    if (!amountStr || isNaN(amountVal) || amountVal <= 0) {
      toast.error('Please enter a valid amount');
      return;
    }

    triggerHaptic('medium');
    const toastId = toast.loading(`Initiating deposit from ${selectedBank.code}...`);

    try {
      // Trigger API Gateway deposit
      await addMoney({ amount: amountVal });
      toast.success(`Successfully added ${formatAmount(amountVal)} to wallet!`, { id: toastId });
      navigate('/wallet');
    } catch (error) {
      console.warn("Failed to add money via API, executing local fallback:", error);
      
      // Fallback local update for showcase demo
      await localAddMoney(user?.id || 'u_local', amountVal);
      toast.success(`Demo Mode: Added ${formatAmount(amountVal)} via ${selectedBank.code}!`, { id: toastId });
      navigate('/wallet');
    }
  };

  const chips = [100, 500, 1000, 2000];

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white select-none">
      {/* Navigation Header */}
      <TopBar title="Add Money to Wallet" />

      <div className="flex-1 p-5 overflow-y-auto no-scrollbar flex flex-col justify-between">
        
        {/* Top Input Screen */}
        <div className="flex flex-col items-center justify-center my-auto py-4">
          <span className="text-[10px] font-bold text-textSecondary uppercase tracking-widest mb-2">
            Enter Amount
          </span>
          <div className="text-4xl font-extrabold amount-font text-white flex items-center justify-center gap-1">
            <span>₹</span>
            <span>{amountStr || '0'}</span>
            <span className="w-1.5 h-8 bg-primary/80 animate-pulse ml-0.5" />
          </div>

          {/* Quick Add Chips */}
          <div className="flex gap-2.5 mt-6 w-full justify-center">
            {chips.map((val) => (
              <button
                key={val}
                onClick={() => handleQuickChip(val)}
                className="px-4 py-2 text-xs font-bold bg-white/[0.03] active:bg-white/10 active:scale-95 border border-white/[0.04] text-white hover:text-white rounded-xl transition-all"
              >
                +{val}
              </button>
            ))}
          </div>

          {/* Connected bank card details */}
          <Card
            variant="glass"
            onClick={() => { triggerHaptic('light'); setIsBankSheetOpen(true); }}
            className="w-full mt-8 p-4 flex justify-between items-center bg-white/[0.02]"
            animate={false}
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/5 flex items-center justify-center text-lg">
                {selectedBank.logo}
              </div>
              <div className="flex flex-col text-left">
                <span className="text-[10px] text-textSecondary font-bold uppercase tracking-wider">
                  Funding Account
                </span>
                <span className="text-xs font-bold text-white tracking-wide">
                  {selectedBank.name}
                </span>
              </div>
            </div>
            <div className="text-[10px] font-bold text-secondary uppercase bg-white/5 border border-white/5 px-2.5 py-1 rounded-lg">
              Change
            </div>
          </Card>
        </div>

        {/* Custom keypad overlay */}
        <AmountKeypad value={amountStr} onChange={setAmountStr} maxAmount={50000} />

        {/* Proceed Action */}
        <div className="mt-4">
          <Button
            onClick={handleDeposit}
            disabled={!amountStr || parseFloat(amountStr) <= 0 || isAddingMoney}
            variant="gradient"
            className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2"
          >
            <Plus className="w-4 h-4" />
            Proceed to Add {amountStr ? formatAmount(parseFloat(amountStr)) : ''}
          </Button>
        </div>
      </div>

      {/* Dynamic Bank account selector bottom sheet drawer */}
      <BottomSheet isOpen={isBankSheetOpen} onClose={() => setIsBankSheetOpen(false)} title="Select Payment Bank">
        <div className="flex flex-col gap-2.5 my-2">
          {MOCK_BANKS.map((bank) => {
            const isSelected = selectedBank.id === bank.id;
            return (
              <div
                key={bank.id}
                onClick={() => {
                  triggerHaptic('medium');
                  setSelectedBank(bank);
                  setIsBankSheetOpen(false);
                }}
                className={`p-4 rounded-2xl flex justify-between items-center cursor-pointer border transition-all ${
                  isSelected
                    ? 'bg-primary/10 border-primary/20 text-white'
                    : 'bg-white/[0.02] border-white/[0.04] text-textSecondary hover:bg-white/[0.04]'
                }`}
              >
                <div className="flex items-center gap-3.5">
                  <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center text-lg">
                    {bank.logo}
                  </div>
                  <div className="flex flex-col text-left">
                    <span className="text-sm font-bold text-white tracking-wide">{bank.name}</span>
                    <span className="text-[9px] text-textSecondary mt-0.5">Mock checking link suffix: {bank.suffix}</span>
                  </div>
                </div>
                {isSelected && (
                  <div className="w-5 h-5 rounded-full bg-success flex items-center justify-center">
                    <Check className="w-3.5 h-3.5 text-white stroke-[3.5]" />
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </BottomSheet>
    </div>
  );
};

export default AddMoneyScreen;
