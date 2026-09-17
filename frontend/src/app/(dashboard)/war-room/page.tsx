'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import {
  ShieldAlert,
  Flame,
  Zap,
  Radio,
  Ban,
  Lock,
  RefreshCw,
  AlertTriangle,
  Activity,
  Power,
  Sliders,
  CheckCircle2,
} from 'lucide-react';
import { useToast } from '@/lib/useToast';
import confetti from 'canvas-confetti';

export default function WarRoomPage() {
  const { toast } = useToast();
  const [defconLevel, setDefconLevel] = useState<1 | 2 | 3 | 4 | 5>(2);
  const [isKillSwitchOpen, setIsKillSwitchOpen] = useState(false);
  const [killSwitchReason, setKillSwitchReason] = useState('High-Velocity UPI Phishing Campaign Detected');
  const [activeChannels, setActiveChannels] = useState({
    upiIntent: true,
    upiCollect: false, // Frozen
    cardAcquiring: true,
    netBanking: true,
  });

  const triggerKillSwitch = () => {
    setIsKillSwitchOpen(false);
    toast('GLOBAL EMERGENCY KILL SWITCH ACTIVATED', 'High-risk payment endpoints rate-limited & blocked.', 'danger');
    confetti({
      particleCount: 80,
      spread: 70,
      origin: { y: 0.2 },
    });
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Live Tactical War Room & Incident Command Center"
        description="High-consequence real-time threat response matrix, DEFCON attack levels, and global emergency kill switch controls."
        breadcrumbs={['SentinelX', 'Operations', 'Tactical War Room']}
        action={
          <Button
            variant="danger"
            size="sm"
            onClick={() => setIsKillSwitchOpen(true)}
            leftIcon={<Power className="h-3.5 w-3.5" />}
          >
            Emergency Kill Switch
          </Button>
        }
      />

      {/* DEFCON Threat Level Selector */}
      <Card className="border-rose-500/40 bg-slate-950 text-white">
        <CardContent className="p-6 space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-4">
            <div>
              <div className="flex items-center gap-2">
                <Radio className="h-5 w-5 text-rose-500 animate-pulse" />
                <h3 className="text-lg font-bold font-mono">GLOBAL THREAT LEVEL STATUS</h3>
              </div>
              <p className="text-xs text-slate-400 mt-1">Select current enterprise DEFCON threat operational state</p>
            </div>

            <Badge
              variant={defconLevel <= 2 ? 'danger' : defconLevel === 3 ? 'warning' : 'success'}
              className="text-sm font-mono px-3 py-1 uppercase"
            >
              DEFCON LEVEL {defconLevel} — {defconLevel === 1 ? 'MAX CRITICAL ATTACK' : defconLevel === 2 ? 'ELEVATED THREAT BURST' : 'STABLE OPERATIONS'}
            </Badge>
          </div>

          {/* DEFCON Level Buttons */}
          <div className="grid grid-cols-2 sm:grid-cols-5 gap-3 font-mono text-xs">
            {([1, 2, 3, 4, 5] as const).map((lvl) => (
              <button
                key={lvl}
                onClick={() => {
                  setDefconLevel(lvl);
                  toast(`DEFCON Level Changed: Level ${lvl}`, `Updated operational readiness protocol to DEFCON ${lvl}.`, lvl <= 2 ? 'danger' : 'info');
                }}
                className={`p-3 rounded-xl border text-center transition-all ${
                  defconLevel === lvl
                    ? lvl <= 2
                      ? 'bg-rose-950 border-rose-500 text-rose-200 ring-2 ring-rose-500/50 scale-105 font-bold'
                      : 'bg-indigo-950 border-indigo-500 text-indigo-200 ring-2 ring-indigo-500/50 scale-105 font-bold'
                    : 'bg-slate-900 border-slate-800 text-slate-400 hover:border-slate-700'
                }`}
              >
                <div className="text-sm font-extrabold">DEFCON {lvl}</div>
                <div className="text-[10px] opacity-80 mt-1">
                  {lvl === 1 ? 'MAX CRITICAL' : lvl === 2 ? 'ELEVATED' : lvl === 3 ? 'GUARDED' : lvl === 4 ? 'ELEVATED RISK' : 'NOMINAL'}
                </div>
              </button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* 2 Column Layout: Attack Vectors & Channel Controls */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Live Attack Stream Ledger */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-bold flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <Flame className="h-4 w-4 text-rose-600 animate-pulse" /> Live Attack Burst Telemetry Stream
                </span>
                <Badge variant="danger" className="font-mono text-[10px]">
                  SUB-SECOND FEED
                </Badge>
              </CardTitle>
              <CardDescription>Detected active threat vectors in past 15 minutes</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3 text-xs">
              {[
                { time: '14:34:10 IST', vector: 'Distributed NordVPN Proxy Burst', target: 'UPI Collect API', intensity: 'HIGH (420 req/s)', action: 'AUTO RATE LIMITED' },
                { time: '14:32:05 IST', vector: 'Mule Cluster #8912 Liquidation Attempt', target: 'HDFC Beneficiary Pool', intensity: 'CRITICAL (₹ 85 Lakh)', action: 'HOLD PENDING' },
                { time: '14:28:44 IST', vector: 'Synthetic Identity Account Creation', target: 'KYC Onboarding Service', intensity: 'MEDIUM', action: 'SELFIE RE-AUTH SENT' },
              ].map((item, i) => (
                <div key={i} className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 p-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 font-mono">
                  <div className="space-y-0.5">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-slate-900 dark:text-slate-100">{item.vector}</span>
                      <Badge variant={item.intensity.includes('CRITICAL') ? 'danger' : 'warning'} size="sm">
                        {item.intensity}
                      </Badge>
                    </div>
                    <span className="text-[11px] text-slate-500 block">Target: {item.target} • {item.time}</span>
                  </div>
                  <Badge variant="success" className="font-mono text-[10px] shrink-0">
                    {item.action}
                  </Badge>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>

        {/* Right 1 Col: Live Channel Control Toggles */}
        <div className="space-y-6">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-bold flex items-center gap-2">
                <Sliders className="h-4 w-4 text-indigo-600 dark:text-indigo-400" /> Channel Enforcement Switches
              </CardTitle>
              <CardDescription>Instant channel throttling controls</CardDescription>
            </CardHeader>

            <CardContent className="space-y-3 text-xs">
              {[
                { key: 'upiIntent', name: 'UPI Intent Payments', desc: 'Active • 1,240 TPS' },
                { key: 'upiCollect', name: 'UPI Collect Requests', desc: 'FROZEN BY EMERGENCY RULE R-109' },
                { key: 'cardAcquiring', name: 'Credit Card Acquiring', desc: 'Active • 420 TPS' },
                { key: 'netBanking', name: 'Net Banking Transfers', desc: 'Active • 180 TPS' },
              ].map((ch) => (
                <div key={ch.key} className="flex items-center justify-between p-3 rounded-lg border border-slate-200 dark:border-slate-800">
                  <div>
                    <span className="font-bold text-slate-900 dark:text-slate-100 block">{ch.name}</span>
                    <span className="text-[10px] text-slate-500 font-mono">{ch.desc}</span>
                  </div>
                  <Badge variant={ch.key === 'upiCollect' ? 'danger' : 'success'}>
                    {ch.key === 'upiCollect' ? 'FROZEN' : 'ACTIVE'}
                  </Badge>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Emergency Kill Switch Confirmation Modal */}
      <Modal isOpen={isKillSwitchOpen} onClose={() => setIsKillSwitchOpen(false)} title="ACTIVATE GLOBAL EMERGENCY KILL SWITCH">
        <div className="space-y-4 text-xs">
          <div className="p-3 rounded-lg bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-900/60 text-red-900 dark:text-red-300">
            <div className="flex items-center gap-2 font-bold mb-1">
              <AlertTriangle className="h-4 w-4 text-red-600" /> WARNING: GLOBAL ENFORCEMENT ACTION
            </div>
            <p className="leading-relaxed">
              Activating the Global Kill Switch will immediately place high-risk UPI collect endpoints and unverified proxy IPs under sub-10ms rate-limiting.
            </p>
          </div>

          <div>
            <label className="font-semibold text-slate-700 dark:text-slate-300 block mb-1">Audit Incident Reason</label>
            <input
              type="text"
              value={killSwitchReason}
              onChange={(e) => setKillSwitchReason(e.target.value)}
              className="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 p-2 text-xs font-mono"
            />
          </div>

          <div className="pt-2 flex justify-end gap-2 border-t border-slate-200 dark:border-slate-800">
            <Button variant="outline" size="sm" onClick={() => setIsKillSwitchOpen(false)}>
              Cancel
            </Button>
            <Button variant="danger" size="sm" onClick={triggerKillSwitch}>
              CONFIRM EMERGENCY KILL SWITCH
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
