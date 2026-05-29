import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, AlertTriangle, ShieldCheck, XCircle, ChevronRight, Sparkles } from 'lucide-react';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';

import { useHaptic } from '../../hooks/useHaptic';
import TopBar from '../../components/layout/TopBar';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';

export const FraudAlertScreen = () => {
  const navigate = useNavigate();
  const triggerHaptic = useHaptic();
  
  const [riskScore, setRiskScore] = useState(84); // 84% Risk

  const handleAuthorize = () => {
    triggerHaptic('heavy');
    toast.success('Authorized override! Processing payment safely...');
    setTimeout(() => {
      navigate('/home');
    }, 1200);
  };

  const handleCancel = () => {
    triggerHaptic('medium');
    toast.error('Payment cancelled for safety');
    navigate('/home');
  };

  return (
    <div className="flex-1 flex flex-col bg-[#0A0A0F] text-white select-none">
      {/* Top Header */}
      <TopBar title="UPI Security Alert" showBack={true} onBack={() => navigate('/home')} />

      <div className="flex-1 p-5 overflow-y-auto no-scrollbar flex flex-col justify-between items-center text-center">
        
        {/* Warning Badge header */}
        <div className="flex flex-col items-center mt-2 w-full">
          <div className="w-14 h-14 rounded-[20px] bg-red-500/10 border border-red-500/20 flex items-center justify-center text-danger mb-4">
            <ShieldAlert className="w-7 h-7" />
          </div>

          <h2 className="text-lg font-black text-white uppercase tracking-wide">
            Payment Transaction Flagged
          </h2>
          <p className="text-xs text-textSecondary mt-1 px-4 leading-snug">
            Our AI-powered Guard has temporarily held a transfer to suspicious@upi
          </p>
        </div>

        {/* Circular Risk visualization */}
        <div className="relative w-40 h-40 flex items-center justify-center my-6">
          
          {/* Animated pulsing outer ring */}
          <motion.div
            animate={{ scale: [1, 1.08, 1] }}
            transition={{ duration: 1.5, repeat: Infinity }}
            className="absolute inset-0 rounded-full border border-danger/10 pointer-events-none"
          />

          <svg className="w-full h-full transform -rotate-90">
            {/* Background ring */}
            <circle
              cx="80"
              cy="80"
              r="64"
              className="stroke-white/5"
              strokeWidth="8"
              fill="transparent"
            />
            {/* Red risk indicator progress ring */}
            <circle
              cx="80"
              cy="80"
              r="64"
              className="stroke-danger shadow-glow-primary"
              strokeWidth="8"
              fill="transparent"
              strokeDasharray={402}
              strokeDashoffset={402 - (402 * riskScore) / 100}
              strokeLinecap="round"
            />
          </svg>
          
          <div className="absolute flex flex-col items-center">
            <span className="text-[9px] font-bold text-textSecondary uppercase tracking-widest">Risk Score</span>
            <span className="text-2xl font-black text-danger amount-font">{riskScore}%</span>
            <span className="text-[8px] font-bold text-danger/80 bg-danger/10 px-2 py-0.5 rounded-md mt-1 uppercase">
              HIGH RISK
            </span>
          </div>
        </div>

        {/* AI risk analysis annotation card */}
        <Card variant="glass" className="p-4.5 border border-red-500/20 bg-red-500/5 text-left w-full">
          <div className="flex items-center gap-1.5 text-danger mb-2.5">
            <Sparkles className="w-4 h-4 fill-current animate-pulse" />
            <h4 className="text-xs font-bold uppercase tracking-widest">AI Security Explainer</h4>
          </div>
          <p className="text-xs text-textSecondary leading-relaxed">
            The target UPI address <span className="text-white font-semibold amount-font">suspicious@upi</span> has received <span className="text-danger font-semibold">14 fraud reports</span> within the last hour. 
            Additionally, this merchant account was created less than 48 hours ago and is exhibiting velocity spikes common to phishing scams.
          </p>
        </Card>

        {/* Action decisions */}
        <div className="w-full flex flex-col gap-3 mt-6">
          <Button
            onClick={handleCancel}
            variant="danger"
            className="w-full py-4 text-sm font-bold flex items-center justify-center gap-2"
          >
            <XCircle className="w-4 h-4" />
            Cancel Payment (Recommended)
          </Button>
          
          <button
            onClick={handleAuthorize}
            className="w-full py-3.5 rounded-2xl bg-white/5 hover:bg-white/10 active:scale-[0.98] border border-white/10 text-xs font-bold text-textSecondary hover:text-white transition-all flex items-center justify-center gap-2"
          >
            <ShieldCheck className="w-4 h-4 text-success" />
            I authorize this payment (Override Block)
          </button>
        </div>
      </div>
    </div>
  );
};

export default FraudAlertScreen;
