'use client';

import React from 'react';
import { Modal } from '@/components/ui/Modal';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Sparkles, Bot, AlertTriangle, ShieldCheck, CheckCircle2 } from 'lucide-react';

export interface ShapFactor {
  feature: string;
  impact: number; // e.g. +32 or -15
  category: 'DEVICE' | 'VELOCITY' | 'GEO' | 'IDENTITY';
  description: string;
}

interface XaiShapModalProps {
  isOpen: boolean;
  onClose: () => void;
  targetId?: string;
  overallScore?: number;
}

const DEFAULT_SHAP_FACTORS: ShapFactor[] = [
  { feature: 'ip.is_vpn_proxy', impact: +32, category: 'GEO', description: 'IP 103.21.124.9 matches known NordVPN exit node hash.' },
  { feature: 'velocity_5m.count', impact: +28, category: 'VELOCITY', description: '6 transactions in 180 seconds (480% above baseline).' },
  { feature: 'device.is_new', impact: +18, category: 'DEVICE', description: 'First observation of Android 14 fingerprint dev_99a8b1.' },
  { feature: 'beneficiary.mule_link', impact: +15, category: 'IDENTITY', description: 'Recipient VPA store@ybl linked to reported mule cluster #892.' },
  { feature: 'kyc.pan_verified', impact: -5, category: 'IDENTITY', description: 'PAN & Aadhaar full KYC verified.' },
];

export function XaiShapModal({
  isOpen,
  onClose,
  targetId = 'TX-89101',
  overallScore = 88,
}: XaiShapModalProps) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Explainable AI (XAI) SHAP Feature Decomposition — ${targetId}`}>
      <div className="space-y-4 text-xs">
        {/* Score Header */}
        <div className="flex items-center justify-between rounded-xl bg-slate-900 text-white p-3.5 border border-slate-800">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-600 text-white font-mono font-bold text-lg">
              {overallScore}
            </div>
            <div>
              <span className="font-bold block">ML Model Risk Score Explanation</span>
              <span className="text-[10px] text-slate-400 font-mono">XGBoost ML Engine v4.2 Inference</span>
            </div>
          </div>

          <Badge variant={overallScore >= 75 ? 'danger' : 'warning'}>
            {overallScore >= 75 ? 'HIGH RISK' : 'REVIEW'}
          </Badge>
        </div>

        {/* SHAP Waterfall Feature Breakdown */}
        <div className="space-y-2">
          <span className="font-mono text-[11px] text-slate-400 uppercase block">SHapley Additive exPlanations (SHAP) Waterfall</span>

          {DEFAULT_SHAP_FACTORS.map((factor) => (
            <div key={factor.feature} className="p-2.5 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 space-y-1">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className="font-mono font-bold text-indigo-600 dark:text-indigo-400">{factor.feature}</span>
                  <Badge variant={factor.impact > 0 ? 'danger' : 'success'} size="sm" className="font-mono">
                    {factor.impact > 0 ? `+${factor.impact} pts` : `${factor.impact} pts`}
                  </Badge>
                </div>
                <span className="text-[10px] font-mono text-slate-400">{factor.category}</span>
              </div>
              <p className="text-[11px] text-slate-600 dark:text-slate-400 leading-snug">{factor.description}</p>
            </div>
          ))}
        </div>

        {/* Counterfactual Reasoning Box */}
        <div className="rounded-xl border border-indigo-200 dark:border-indigo-900/60 bg-indigo-50/50 dark:bg-indigo-950/30 p-3 text-indigo-900 dark:text-indigo-300">
          <div className="flex items-center gap-1.5 font-bold mb-1">
            <Sparkles className="h-4 w-4 text-indigo-600" /> Counterfactual Reasoning Engine
          </div>
          <p className="text-[11px] leading-relaxed">
            If the customer completes 3D-Secure Biometric Verification on their primary device, the projected risk score drops from <strong>88</strong> to <strong>32 (LOW RISK)</strong>.
          </p>
        </div>
      </div>
    </Modal>
  );
}
