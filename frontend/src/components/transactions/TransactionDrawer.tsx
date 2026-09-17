'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import {
  X,
  ShieldAlert,
  ShieldCheck,
  Ban,
  CheckCircle,
  ExternalLink,
  Smartphone,
  MapPin,
  Clock,
  User,
  CreditCard,
  Building2,
  FileText,
  AlertTriangle,
  ArrowRight,
  MessageSquare,
  Share2,
} from 'lucide-react';
import { Drawer } from '@/components/ui/Drawer';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Tabs } from '@/components/ui/Tabs';
import { RiskScoreBreakdown } from '@/components/ui/RiskScoreBreakdown';
import { useToast } from '@/lib/useToast';

export interface TransactionDetail {
  id: string;
  senderName: string;
  senderId: string;
  senderAccount: string;
  recipientName: string;
  recipientId: string;
  recipientAccount: string;
  amount: number;
  currency: string;
  status: 'SUCCESS' | 'BLOCKED' | 'FLAGGED' | 'PENDING' | 'REJECTED';
  riskScore: number;
  riskDecision: 'ALLOW' | 'BLOCK' | 'FLAG' | 'REVIEW';
  timestamp: string;
  paymentMethod: string;
  ipAddress: string;
  location: string;
  deviceFingerprint: string;
  deviceOs: string;
  vpnProxy: boolean;
  velocity1h: number;
  triggeredRules: { code: string; name: string; severity: 'HIGH' | 'MEDIUM' | 'LOW' }[];
  notes?: { author: string; time: string; text: string }[];
}

interface TransactionDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  transaction: TransactionDetail | null;
  onActionComplete?: () => void;
}

export function TransactionDrawer({
  isOpen,
  onClose,
  transaction,
  onActionComplete,
}: TransactionDrawerProps) {
  const { toast } = useToast();
  const [activeTab, setActiveTab] = useState<'overview' | 'device' | 'graph' | 'rules' | 'notes'>('overview');
  const [newNote, setNewNote] = useState('');
  const [localNotes, setLocalNotes] = useState<{ author: string; time: string; text: string }[]>([]);

  if (!transaction) return null;

  const handleAction = (action: 'ALLOW' | 'BLOCK' | 'ESCALATE' | 'FLAG_MULE') => {
    let msg = '';
    if (action === 'ALLOW') msg = `Transaction ${transaction.id} approved & whitelisted.`;
    if (action === 'BLOCK') msg = `Transaction ${transaction.id} blocked & account placed on freeze review.`;
    if (action === 'ESCALATE') msg = `Case escalated to Senior Risk Investigator.`;
    if (action === 'FLAG_MULE') msg = `Beneficiary ${transaction.recipientName} tagged in Mule Network DB.`;

    toast(`Action Applied: ${action}`, msg, action === 'ALLOW' ? 'success' : 'warning');

    if (onActionComplete) onActionComplete();
  };

  const handleAddNote = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newNote.trim()) return;
    const noteObj = {
      author: 'Current Analyst (You)',
      time: 'Just now',
      text: newNote.trim(),
    };
    setLocalNotes([noteObj, ...localNotes]);
    setNewNote('');
    toast('Note added', 'Analyst investigation note recorded.', 'info');
  };

  const allNotes = [...localNotes, ...(transaction.notes || [])];

  return (
    <Drawer isOpen={isOpen} onClose={onClose} title={`Transaction Investigation — ${transaction.id}`}>
      <div className="space-y-6">
        {/* Top Header Card */}
        <div className="rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/70 p-4">
          <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-200 dark:border-slate-800 pb-3">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xl font-bold font-mono text-slate-900 dark:text-slate-100">
                  ₹{transaction.amount.toLocaleString('en-IN')} {transaction.currency}
                </span>
                <Badge
                  variant={
                    transaction.status === 'BLOCKED' || transaction.status === 'REJECTED'
                      ? 'danger'
                      : transaction.status === 'FLAGGED'
                      ? 'warning'
                      : 'success'
                  }
                >
                  {transaction.status}
                </Badge>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 flex items-center gap-1.5 font-mono">
                <Clock className="h-3.5 w-3.5" />
                {transaction.timestamp}
              </p>
            </div>

            {/* Direct Quick Actions */}
            <div className="flex items-center gap-2">
              <Button
                size="sm"
                variant="outline"
                className="border-emerald-300 text-emerald-700 hover:bg-emerald-50 dark:border-emerald-800 dark:text-emerald-400 dark:hover:bg-emerald-950/40 text-xs"
                onClick={() => handleAction('ALLOW')}
              >
                <CheckCircle className="h-3.5 w-3.5 mr-1" />
                Allow
              </Button>
              <Button
                size="sm"
                variant="outline"
                className="border-red-300 text-red-700 hover:bg-red-50 dark:border-red-800 dark:text-red-400 dark:hover:bg-red-950/40 text-xs"
                onClick={() => handleAction('BLOCK')}
              >
                <Ban className="h-3.5 w-3.5 mr-1" />
                Block
              </Button>
              <Button
                size="sm"
                variant="primary"
                className="text-xs"
                onClick={() => handleAction('ESCALATE')}
              >
                <ShieldAlert className="h-3.5 w-3.5 mr-1" />
                Escalate Case
              </Button>
            </div>
          </div>

          {/* Party Flow Summary */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-3 text-xs">
            <div className="space-y-1">
              <span className="text-[11px] font-mono text-slate-400 uppercase block">Sender (Originator)</span>
              <Link
                href={`/customers/${transaction.senderId}`}
                className="font-medium text-indigo-600 dark:text-indigo-400 hover:underline flex items-center gap-1"
              >
                <User className="h-3.5 w-3.5" />
                {transaction.senderName}
                <ExternalLink className="h-3 w-3" />
              </Link>
              <p className="font-mono text-slate-500 dark:text-slate-400">{transaction.senderAccount}</p>
            </div>

            <div className="space-y-1">
              <span className="text-[11px] font-mono text-slate-400 uppercase block">Recipient (Beneficiary)</span>
              <div className="font-medium text-slate-900 dark:text-slate-100 flex items-center gap-1">
                <Building2 className="h-3.5 w-3.5 text-slate-400" />
                {transaction.recipientName}
              </div>
              <p className="font-mono text-slate-500 dark:text-slate-400">{transaction.recipientAccount}</p>
            </div>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="border-b border-slate-200 dark:border-slate-800 flex gap-4 text-xs font-medium">
          <button
            onClick={() => setActiveTab('overview')}
            className={`pb-2 border-b-2 transition-colors ${
              activeTab === 'overview'
                ? 'border-indigo-600 text-indigo-600 dark:text-indigo-400 font-semibold'
                : 'border-transparent text-slate-500 hover:text-slate-700 dark:text-slate-400'
            }`}
          >
            Risk Overview
          </button>
          <button
            onClick={() => setActiveTab('device')}
            className={`pb-2 border-b-2 transition-colors ${
              activeTab === 'device'
                ? 'border-indigo-600 text-indigo-600 dark:text-indigo-400 font-semibold'
                : 'border-transparent text-slate-500 hover:text-slate-700 dark:text-slate-400'
            }`}
          >
            Device & Network
          </button>
          <button
            onClick={() => setActiveTab('graph')}
            className={`pb-2 border-b-2 transition-colors ${
              activeTab === 'graph'
                ? 'border-indigo-600 text-indigo-600 dark:text-indigo-400 font-semibold'
                : 'border-transparent text-slate-500 hover:text-slate-700 dark:text-slate-400'
            }`}
          >
            Entity Graph
          </button>
          <button
            onClick={() => setActiveTab('rules')}
            className={`pb-2 border-b-2 transition-colors ${
              activeTab === 'rules'
                ? 'border-indigo-600 text-indigo-600 dark:text-indigo-400 font-semibold'
                : 'border-transparent text-slate-500 hover:text-slate-700 dark:text-slate-400'
            }`}
          >
            Rules Triggered ({transaction.triggeredRules.length})
          </button>
          <button
            onClick={() => setActiveTab('notes')}
            className={`pb-2 border-b-2 transition-colors ${
              activeTab === 'notes'
                ? 'border-indigo-600 text-indigo-600 dark:text-indigo-400 font-semibold'
                : 'border-transparent text-slate-500 hover:text-slate-700 dark:text-slate-400'
            }`}
          >
            Notes ({allNotes.length})
          </button>
        </div>

        {/* Tab 1: Overview & Risk Breakdown */}
        {activeTab === 'overview' && (
          <div className="space-y-4">
            <RiskScoreBreakdown overallScore={transaction.riskScore} decision={transaction.riskDecision} />

            {/* Quick Context Summary */}
            <div className="rounded-lg border border-amber-200 dark:border-amber-900/50 bg-amber-50/50 dark:bg-amber-950/20 p-3 text-xs text-amber-900 dark:text-amber-300">
              <div className="flex items-center gap-1.5 font-semibold mb-1">
                <AlertTriangle className="h-4 w-4 text-amber-600 shrink-0" />
                <span>AI Automated Anomaly Summary</span>
              </div>
              <p className="leading-relaxed text-amber-800 dark:text-amber-400">
                Transaction flagged due to rapid velocity burst (6 payments in 3 mins) from a newly bound Android 14 device using a known VPN exit node in Mumbai while GPS reports Delhi.
              </p>
            </div>
          </div>
        )}

        {/* Tab 2: Device & Network Intelligence */}
        {activeTab === 'device' && (
          <div className="space-y-4 text-xs">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div className="rounded-lg border border-slate-200 dark:border-slate-800 p-3 space-y-2">
                <span className="font-mono text-[11px] text-slate-400 uppercase block">IP & Location</span>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">IP Address:</span>
                  <span className="font-mono font-medium">{transaction.ipAddress}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Location:</span>
                  <span className="font-medium">{transaction.location}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Proxy / VPN:</span>
                  <Badge variant={transaction.vpnProxy ? 'danger' : 'success'}>
                    {transaction.vpnProxy ? 'VPN Detected' : 'Clean IP'}
                  </Badge>
                </div>
              </div>

              <div className="rounded-lg border border-slate-200 dark:border-slate-800 p-3 space-y-2">
                <span className="font-mono text-[11px] text-slate-400 uppercase block">Device Telemetry</span>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Device OS:</span>
                  <span className="font-medium">{transaction.deviceOs}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Fingerprint Hash:</span>
                  <span className="font-mono text-[11px] text-slate-600 dark:text-slate-400 truncate max-w-[120px]">
                    {transaction.deviceFingerprint}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">1-Hour Velocity:</span>
                  <span className="font-mono font-bold text-amber-600">{transaction.velocity1h} Txns</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Tab 3: Entity Graph */}
        {activeTab === 'graph' && (
          <div className="space-y-4 text-xs">
            <div className="rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 p-4 text-center">
              <Share2 className="h-8 w-8 text-indigo-500 mx-auto mb-2" />
              <h5 className="font-semibold text-slate-900 dark:text-slate-100">Network Entity Connection</h5>
              <p className="text-slate-500 dark:text-slate-400 max-w-md mx-auto text-xs mt-1">
                Sender account shares IP <span className="font-mono text-slate-700 dark:text-slate-300">103.21.124.9</span> with 2 previously flagged mule accounts.
              </p>
              <div className="mt-4">
                <Link
                  href={`/network?entity=${transaction.senderId}`}
                  className="inline-flex items-center gap-1 text-xs font-semibold text-indigo-600 dark:text-indigo-400 hover:underline"
                >
                  Open Full Graph Visualizer <ArrowRight className="h-3.5 w-3.5" />
                </Link>
              </div>
            </div>
          </div>
        )}

        {/* Tab 4: Rules Triggered */}
        {activeTab === 'rules' && (
          <div className="space-y-3">
            {transaction.triggeredRules.map((rule) => (
              <div
                key={rule.code}
                className="flex items-start justify-between rounded-lg border border-slate-200 dark:border-slate-800 p-3 bg-white dark:bg-slate-900"
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs font-bold text-indigo-600 dark:text-indigo-400">
                      {rule.code}
                    </span>
                    <span className="text-xs font-semibold text-slate-900 dark:text-slate-100">
                      {rule.name}
                    </span>
                  </div>
                </div>
                <Badge variant={rule.severity === 'HIGH' ? 'danger' : 'warning'}>
                  {rule.severity} SEVERITY
                </Badge>
              </div>
            ))}
          </div>
        )}

        {/* Tab 5: Analyst Notes */}
        {activeTab === 'notes' && (
          <div className="space-y-4">
            <form onSubmit={handleAddNote} className="space-y-2">
              <textarea
                value={newNote}
                onChange={(e) => setNewNote(e.target.value)}
                placeholder="Add investigation note or reason for action..."
                className="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 p-2.5 text-xs text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                rows={3}
              />
              <Button type="submit" size="sm" className="text-xs">
                <MessageSquare className="h-3.5 w-3.5 mr-1" /> Save Note
              </Button>
            </form>

            <div className="space-y-3">
              {allNotes.length === 0 ? (
                <p className="text-xs text-slate-500 text-center py-4">No notes recorded yet.</p>
              ) : (
                allNotes.map((note, index) => (
                  <div
                    key={index}
                    className="rounded-lg border border-slate-100 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/60 p-3 text-xs space-y-1"
                  >
                    <div className="flex items-center justify-between text-slate-500">
                      <span className="font-semibold text-slate-700 dark:text-slate-300">{note.author}</span>
                      <span className="font-mono text-[10px] text-slate-400">{note.time}</span>
                    </div>
                    <p className="text-slate-800 dark:text-slate-200">{note.text}</p>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>
    </Drawer>
  );
}
