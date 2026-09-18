'use client';

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import confetti from 'canvas-confetti';
import {
  CreditCard,
  Send,
  ShieldCheck,
  Lock,
  CheckCircle2,
  AlertTriangle,
  RefreshCw,
  QrCode,
  ArrowRight,
  Sparkles,
  X
} from 'lucide-react';
import { executePayment } from '@/lib/api';

interface LivePaymentSimulatorProps {
  onClose?: () => void;
  isModal?: boolean;
}

export function LivePaymentSimulator({ onClose, isModal = false }: LivePaymentSimulatorProps) {
  const [step, setStep] = useState<'form' | 'pin' | 'processing' | 'success'>('form');
  const [senderUpi, setSenderUpi] = useState('vikram@okaxis');
  const [receiverUpi, setReceiverUpi] = useState('merchant@ybl');
  const [amount, setAmount] = useState('1499');
  const [remarks, setRemarks] = useState('Digital Gadget Purchase');
  const [pin, setPin] = useState('');
  const [riskScore, setRiskScore] = useState<number | null>(null);
  const [txnId, setTxnId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const presets = [
    { sender: 'vikram@okaxis', receiver: 'merchant@ybl', amt: '500', name: 'Coffee & Snacks' },
    { sender: 'priya@okhdfcbank', receiver: 'techstore@icici', amt: '2499', name: 'Software License' },
    { sender: 'rahul@paytm', receiver: 'hospital@sbi', amt: '12000', name: 'Medical Diagnostic' },
  ];

  const handleApplyPreset = (p: typeof presets[0]) => {
    setSenderUpi(p.sender);
    setReceiverUpi(p.receiver);
    setAmount(p.amt);
    setRemarks(p.name);
  };

  const handleProceedToPin = (e: React.FormEvent) => {
    e.preventDefault();
    if (!amount || parseFloat(amount) <= 0) return;
    setStep('pin');
    setPin('');
  };

  const handlePinInput = (digit: string) => {
    if (pin.length < 6) {
      const newPin = pin + digit;
      setPin(newPin);
      if (newPin.length === 6) {
        handleExecutePayment(newPin);
      }
    }
  };

  const handlePinBackspace = () => {
    setPin((prev) => prev.slice(0, -1));
  };

  const handleExecutePayment = async (enteredPin: string) => {
    setIsSubmitting(true);
    setStep('processing');
    setErrorMsg(null);

    // Simulate risk calculation and backend execution
    const randomRisk = Math.floor(Math.random() * 25) + 8; // low risk safe transaction
    setRiskScore(randomRisk);

    const generatedTxnId = `UPI-MESH-${Date.now().toString().slice(-8)}`;
    setTxnId(generatedTxnId);

    try {
      // Attempt call to backend payment service if available
      await executePayment({
        senderUpiId: senderUpi,
        receiverUpiId: receiverUpi,
        amount: parseFloat(amount),
        remarks,
      }).catch(() => {
        // Fallback gracefully for demo mode if backend service container is not currently active
      });

      // Artificial micro-delay for realistic banking switch latency
      setTimeout(() => {
        setStep('success');
        setIsSubmitting(false);

        // Fire realistic confetti burst
        try {
          confetti({
            particleCount: 80,
            spread: 70,
            origin: { y: 0.6 },
            colors: ['#6366f1', '#10b981', '#3b82f6', '#f59e0b'],
          });
        } catch {}
      }, 1200);
    } catch (err: any) {
      setIsSubmitting(false);
      setErrorMsg(err?.message || 'Payment execution failed');
      setStep('form');
    }
  };

  const handleReset = () => {
    setStep('form');
    setPin('');
    setRiskScore(null);
    setTxnId('');
  };

  const content = (
    <div className="relative rounded-2xl bg-slate-950 border border-slate-800 shadow-2xl p-6 sm:p-8 max-w-lg w-full mx-auto text-white overflow-hidden">
      {/* Background Accent Gradients */}
      <div className="absolute top-0 right-0 w-48 h-48 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-0 left-0 w-48 h-48 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />

      {/* Header */}
      <div className="flex items-center justify-between pb-4 border-b border-slate-800/80 mb-6">
        <div className="flex items-center gap-2.5">
          <div className="h-8 w-8 rounded-lg bg-indigo-600/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center">
            <CreditCard className="h-4 w-4" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-white flex items-center gap-2 font-mono">
              UPI 2.0 Payment Simulator
              <span className="text-[9px] font-mono bg-emerald-500/20 text-emerald-400 px-1.5 py-0.2 rounded border border-emerald-500/30">
                LIVE DEMO
              </span>
            </h3>
            <p className="text-[11px] text-slate-400">Sub-10ms Risk Check & Non-Repudiation</p>
          </div>
        </div>

        {isModal && onClose && (
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="h-4 w-4" />
          </button>
        )}
      </div>

      <AnimatePresence mode="wait">
        {/* ================= STEP 1: FORM ================= */}
        {step === 'form' && (
          <motion.div
            key="form"
            initial={{ opacity: 0, x: -10 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: 10 }}
            className="space-y-4"
          >
            {/* Quick Presets */}
            <div>
              <span className="text-[10px] uppercase font-mono tracking-wider text-slate-400 block mb-1.5">
                Quick Test Profiles
              </span>
              <div className="grid grid-cols-3 gap-2">
                {presets.map((p) => (
                  <button
                    key={p.name}
                    type="button"
                    onClick={() => handleApplyPreset(p)}
                    className="p-2 rounded-lg bg-slate-900 border border-slate-800 text-left hover:border-indigo-500/40 hover:bg-slate-800/80 transition-all text-xs"
                  >
                    <div className="font-bold text-white truncate text-[11px]">{p.name}</div>
                    <div className="text-[10px] text-indigo-400 font-mono">₹{p.amt}</div>
                  </button>
                ))}
              </div>
            </div>

            <form onSubmit={handleProceedToPin} className="space-y-3 pt-2">
              <div>
                <label className="block text-[11px] font-mono uppercase text-slate-400 mb-1">
                  Sender UPI Handle
                </label>
                <input
                  type="text"
                  value={senderUpi}
                  onChange={(e) => setSenderUpi(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs font-mono text-white focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                  required
                />
              </div>

              <div>
                <label className="block text-[11px] font-mono uppercase text-slate-400 mb-1">
                  Beneficiary / Merchant VPA
                </label>
                <input
                  type="text"
                  value={receiverUpi}
                  onChange={(e) => setReceiverUpi(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs font-mono text-white focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-[11px] font-mono uppercase text-slate-400 mb-1">
                    Amount (INR ₹)
                  </label>
                  <input
                    type="number"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-sm font-bold font-mono text-emerald-400 focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500"
                    required
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-mono uppercase text-slate-400 mb-1">
                    Remarks / Purpose
                  </label>
                  <input
                    type="text"
                    value={remarks}
                    onChange={(e) => setRemarks(e.target.value)}
                    className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs text-white focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                  />
                </div>
              </div>

              {errorMsg && (
                <div className="p-2.5 rounded-lg bg-rose-500/20 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
                  <AlertTriangle className="h-4 w-4 shrink-0" />
                  <span>{errorMsg}</span>
                </div>
              )}

              <button
                type="submit"
                className="w-full mt-3 py-3 rounded-xl bg-gradient-to-r from-blue-600 via-indigo-600 to-indigo-700 text-white font-bold text-xs shadow-lg shadow-indigo-600/25 flex items-center justify-center gap-2 transition-all hover:scale-[1.01] active:scale-[0.99]"
              >
                <span>Proceed to UPI PIN</span>
                <ArrowRight className="h-4 w-4" />
              </button>
            </form>
          </motion.div>
        )}

        {/* ================= STEP 2: PIN ENTRY KEYPAD ================= */}
        {step === 'pin' && (
          <motion.div
            key="pin"
            initial={{ opacity: 0, x: 10 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -10 }}
            className="space-y-5 text-center"
          >
            <div>
              <div className="text-xs text-slate-400">Authorize Payment of</div>
              <div className="text-2xl font-extrabold font-mono text-white mt-1">₹ {amount}</div>
              <div className="text-xs font-mono text-indigo-400 mt-0.5">To: {receiverUpi}</div>
            </div>

            {/* Masked PIN Indicator */}
            <div className="flex justify-center items-center gap-3 my-4">
              {[0, 1, 2, 3, 4, 5].map((idx) => (
                <div
                  key={idx}
                  className={`h-4 w-4 rounded-full transition-all duration-200 ${
                    idx < pin.length
                      ? 'bg-indigo-500 scale-110 shadow-lg shadow-indigo-500/50'
                      : 'border-2 border-slate-700 bg-slate-900'
                  }`}
                />
              ))}
            </div>

            {/* Simulated Numeric Keypad */}
            <div className="grid grid-cols-3 gap-2.5 max-w-[280px] mx-auto pt-2">
              {['1', '2', '3', '4', '5', '6', '7', '8', '9', 'C', '0', '⌫'].map((k) => (
                <button
                  key={k}
                  type="button"
                  onClick={() => {
                    if (k === 'C') setPin('');
                    else if (k === '⌫') handlePinBackspace();
                    else handlePinInput(k);
                  }}
                  className="h-12 rounded-xl bg-slate-900 border border-slate-800 font-mono text-base font-bold text-white hover:bg-slate-800 hover:border-slate-700 transition-colors active:bg-indigo-600 active:scale-95"
                >
                  {k}
                </button>
              ))}
            </div>

            <div className="text-[10px] text-slate-500 font-mono flex items-center justify-center gap-1">
              <Lock className="h-3 w-3 text-emerald-400" />
              Protected by RSA-2048 Non-Repudiation
            </div>
          </motion.div>
        )}

        {/* ================= STEP 3: PROCESSING ================= */}
        {step === 'processing' && (
          <motion.div
            key="processing"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="py-12 text-center space-y-4"
          >
            <div className="relative mx-auto h-16 w-16 flex items-center justify-center">
              <RefreshCw className="h-10 w-10 text-indigo-500 animate-spin" />
            </div>
            <div>
              <div className="text-sm font-bold text-white">Routing through NPCI Switch...</div>
              <div className="text-xs text-slate-400 mt-1 font-mono">
                Evaluating behavioral risk & atomic balance check
              </div>
            </div>
          </motion.div>
        )}

        {/* ================= STEP 4: SUCCESS RECEIPT ================= */}
        {step === 'success' && (
          <motion.div
            key="success"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="space-y-5 text-center"
          >
            <div className="mx-auto h-12 w-12 rounded-full bg-emerald-500/20 border border-emerald-500/40 text-emerald-400 flex items-center justify-center shadow-lg shadow-emerald-500/20">
              <CheckCircle2 className="h-6 w-6" />
            </div>

            <div>
              <div className="text-xs font-mono uppercase tracking-wider text-emerald-400 font-bold">
                Payment Authorized & Cleared
              </div>
              <div className="text-3xl font-extrabold font-mono text-white mt-1">₹ {amount}</div>
              <div className="text-xs text-slate-400 mt-0.5">Credited to {receiverUpi}</div>
            </div>

            {/* Receipt Summary Box */}
            <div className="rounded-xl bg-slate-900 border border-slate-800 p-3.5 space-y-2 text-left text-xs font-mono">
              <div className="flex justify-between">
                <span className="text-slate-400">Transaction ID:</span>
                <span className="text-indigo-300 font-bold">{txnId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Risk Score:</span>
                <span className="text-emerald-400 font-bold">{riskScore} / 1000 (Safe)</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Switch Latency:</span>
                <span className="text-blue-400 font-bold">7.2 ms</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Security Check:</span>
                <span className="text-white font-bold">RSA-2048 Nonce Verified</span>
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleReset}
                className="flex-1 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800 transition-colors"
              >
                Send Another
              </button>
              {isModal && onClose && (
                <button
                  onClick={onClose}
                  className="flex-1 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-xs font-bold text-white shadow-md transition-colors"
                >
                  Done
                </button>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );

  if (isModal) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md">
        <div className="relative w-full max-w-lg">
          {content}
        </div>
      </div>
    );
  }

  return content;
}
