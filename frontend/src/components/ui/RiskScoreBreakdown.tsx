'use client';

import React from 'react';
import { AlertTriangle, ShieldCheck, ShieldAlert, Smartphone, MapPin, Zap, UserCheck, CreditCard, Share2 } from 'lucide-react';
import { Badge } from '@/components/ui/Badge';

export interface RiskSignal {
  category: 'device' | 'geofence' | 'velocity' | 'behavior' | 'payment' | 'network';
  name: string;
  score: number; // 0 to 100
  status: 'safe' | 'warning' | 'critical';
  details: string;
}

export interface RiskScoreBreakdownProps {
  overallScore: number; // 0 to 100
  decision?: 'ALLOW' | 'BLOCK' | 'FLAG' | 'REVIEW';
  signals?: RiskSignal[];
  showDetails?: boolean;
  className?: string;
}

const DEFAULT_SIGNALS: RiskSignal[] = [
  {
    category: 'device',
    name: 'Device & Identity Fingerprint',
    score: 85,
    status: 'critical',
    details: 'New device (Android 14) combined with proxy IP and VPN usage.',
  },
  {
    category: 'velocity',
    name: 'Transaction Velocity',
    score: 92,
    status: 'critical',
    details: '6 rapid payments within 3 minutes; 4.8x higher than 30-day average.',
  },
  {
    category: 'geofence',
    name: 'Geographic Distance',
    score: 74,
    status: 'warning',
    details: 'IP location (Mumbai) diverges from physical GPS device location (Delhi).',
  },
  {
    category: 'behavior',
    name: 'Biometric & Behavioral Pattern',
    score: 42,
    status: 'warning',
    details: 'High paste frequency detected in beneficiary field.',
  },
  {
    category: 'payment',
    name: 'Payment Instrument Risk',
    score: 15,
    status: 'safe',
    details: 'UPI VPA linked to verified bank account (HDFC Bank).',
  },
  {
    category: 'network',
    name: 'Entity Graph Linkage',
    score: 88,
    status: 'critical',
    details: 'Beneficiary account linked to 3 reported mule accounts in graph cluster.',
  },
];

export function RiskScoreBreakdown({
  overallScore,
  decision = overallScore >= 70 ? 'BLOCK' : overallScore >= 40 ? 'REVIEW' : 'ALLOW',
  signals = DEFAULT_SIGNALS,
  showDetails = true,
  className = '',
}: RiskScoreBreakdownProps) {
  const getScoreColor = (score: number) => {
    if (score >= 75) return 'text-red-600 dark:text-red-400 border-red-500 bg-red-50 dark:bg-red-950/40';
    if (score >= 45) return 'text-amber-600 dark:text-amber-400 border-amber-500 bg-amber-50 dark:bg-amber-950/40';
    return 'text-emerald-600 dark:text-emerald-400 border-emerald-500 bg-emerald-50 dark:bg-emerald-950/40';
  };

  const getBarColor = (score: number) => {
    if (score >= 75) return 'bg-red-600 dark:bg-red-500';
    if (score >= 45) return 'bg-amber-500 dark:bg-amber-400';
    return 'bg-emerald-500 dark:bg-emerald-400';
  };

  const getCategoryIcon = (category: RiskSignal['category']) => {
    switch (category) {
      case 'device':
        return <Smartphone className="h-4 w-4 text-blue-500" />;
      case 'geofence':
        return <MapPin className="h-4 w-4 text-emerald-500" />;
      case 'velocity':
        return <Zap className="h-4 w-4 text-amber-500" />;
      case 'behavior':
        return <UserCheck className="h-4 w-4 text-purple-500" />;
      case 'payment':
        return <CreditCard className="h-4 w-4 text-indigo-500" />;
      case 'network':
        return <Share2 className="h-4 w-4 text-red-500" />;
    }
  };

  return (
    <div className={`space-y-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-4 shadow-sm ${className}`}>
      {/* Top Header: Score & Decision */}
      <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-3">
        <div className="flex items-center gap-3">
          <div className={`flex h-14 w-14 items-center justify-center rounded-xl border-2 text-xl font-bold font-mono ${getScoreColor(overallScore)}`}>
            {overallScore}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h4 className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                Risk Engine Score
              </h4>
              <Badge
                variant={
                  decision === 'BLOCK' ? 'danger' : decision === 'REVIEW' ? 'warning' : 'success'
                }
              >
                {decision}
              </Badge>
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Evaluated across 6 intelligence vectors (Sardine-style model v4.2)
            </p>
          </div>
        </div>

        <div className="text-right hidden sm:block">
          <span className="text-xs font-mono text-slate-400 block">RISK TIER</span>
          <span className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase">
            {overallScore >= 75 ? 'Critical Risk' : overallScore >= 45 ? 'Elevated Risk' : 'Low Risk'}
          </span>
        </div>
      </div>

      {/* Breakdown Progress Bars */}
      <div className="space-y-3">
        <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 font-medium">
          <span>SIGNAL DECOMPOSITION</span>
          <span>SCORE / IMPACT</span>
        </div>

        {signals.map((signal) => (
          <div key={signal.name} className="space-y-1">
            <div className="flex items-center justify-between text-xs">
              <div className="flex items-center gap-2 text-slate-700 dark:text-slate-300 font-medium">
                {getCategoryIcon(signal.category)}
                <span>{signal.name}</span>
              </div>
              <span className="font-mono text-slate-600 dark:text-slate-300 font-semibold">
                {signal.score}/100
              </span>
            </div>
            {/* Bar */}
            <div className="h-1.5 w-full rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-500 ${getBarColor(signal.score)}`}
                style={{ width: `${signal.score}%` }}
              />
            </div>
            {showDetails && (
              <p className="text-[11px] text-slate-500 dark:text-slate-400 pl-6 pt-0.5 leading-snug">
                {signal.details}
              </p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
