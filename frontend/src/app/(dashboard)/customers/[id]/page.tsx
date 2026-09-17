'use client';

import React, { use, useState } from 'react';
import Link from 'next/link';
import {
  ArrowLeft,
  UserCheck,
  ShieldCheck,
  Lock,
  CreditCard,
  Building2,
  Smartphone,
  MapPin,
  Clock,
  ExternalLink,
  ShieldAlert,
  Ban,
  CheckCircle,
  Share2,
  FileText,
  AlertTriangle,
  History,
  Users,
} from 'lucide-react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { StatCard } from '@/components/ui/StatCard';
import { Tabs } from '@/components/ui/Tabs';
import { DataTable } from '@/components/ui/DataTable';
import { RiskScoreBreakdown } from '@/components/ui/RiskScoreBreakdown';
import { TransactionDrawer, TransactionDetail } from '@/components/transactions/TransactionDrawer';
import { useToast } from '@/lib/useToast';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function CustomerDetailPage({ params }: PageProps) {
  const { id } = use(params);
  const { toast } = useToast();
  const [activeTab, setActiveTab] = useState('overview');

  // Time Travel Slider State (Days ago)
  const [timeTravelDays, setTimeTravelDays] = useState<number>(0);

  // Side Drawer state
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [selectedTx, setSelectedTx] = useState<TransactionDetail | null>(null);

  const customerData = {
    id,
    name: 'Anand Kumar Sharma',
    email: 'anand.sharma@domain.com',
    phone: '+91 98765 43210',
    kycStatus: 'FULL_KYC (PAN + Aadhaar Verified)',
    accountAgeDays: 420,
    walletBalance: 145000,
    totalVolume30d: 840000,
    riskScore: 88,
    riskDecision: 'BLOCK' as const,
    vpaHandles: ['anand@okaxis', 'anand.sharma@paytm', 'anand@ybl'],
    bankAccounts: [
      { bank: 'HDFC Bank', accountNo: '•••• 8912', ifsc: 'HDFC0000240', status: 'VERIFIED' },
      { bank: 'Axis Bank', accountNo: '•••• 4410', ifsc: 'UTIB0000109', status: 'VERIFIED' },
    ],
    devices: [
      { id: 'dev_99a8b1', os: 'Android 14 (Samsung S23)', firstSeen: '2025-11-10', vpnUsed: true, trust: 'LOW' },
      { id: 'dev_11c4e7', os: 'Windows 11 Chrome 122', firstSeen: '2024-02-15', vpnUsed: false, trust: 'HIGH' },
    ],
  };

  const familyMembers = [
    { name: 'Sunita Sharma', relation: 'Spouse', accountNo: '•••• 3310', riskScore: 18 },
    { name: 'Rohan Sharma', relation: 'Son', accountNo: '•••• 9901', riskScore: 74 },
  ];

  const sampleTransactions = [
    { id: 'TX-89101', senderUpiId: 'anand@okaxis', receiverUpiId: 'store@ybl', amount: 85000, riskScore: 92, status: 'FLAGGED', timestamp: Date.now() - 60000 },
    { id: 'TX-89099', senderUpiId: 'anand@okaxis', receiverUpiId: 'bazaar@sbi', amount: 14000, riskScore: 68, status: 'FLAGGED', timestamp: Date.now() - 180000 },
    { id: 'TX-89092', senderUpiId: 'anand@okaxis', receiverUpiId: 'cafe@hdfc', amount: 3200, riskScore: 15, status: 'COMPLETED', timestamp: Date.now() - 86400000 },
  ];

  const handleAction = (action: string) => {
    toast(`Action Triggered: ${action}`, `Updated security enforcement rules for customer ${id}.`, action.includes('Freeze') ? 'warning' : 'info');
  };

  const openDrawer = (txRow: any) => {
    setSelectedTx({
      id: txRow.id,
      senderName: customerData.name,
      senderId: customerData.id,
      senderAccount: txRow.senderUpiId,
      recipientName: 'Store Merchant Ltd',
      recipientId: 'MERCH-9902',
      recipientAccount: txRow.receiverUpiId,
      amount: txRow.amount,
      currency: 'INR',
      status: txRow.status,
      riskScore: txRow.riskScore,
      riskDecision: txRow.riskScore >= 75 ? 'BLOCK' : 'REVIEW',
      timestamp: 'Today at 14:32:05 IST',
      paymentMethod: 'UPI Intent',
      ipAddress: '103.21.124.9',
      location: 'Mumbai, MH, India',
      deviceFingerprint: 'fp_99a8b1c4e7',
      deviceOs: 'Android 14',
      vpnProxy: true,
      velocity1h: 6,
      triggeredRules: [{ code: 'R-109', name: 'High Velocity Burst', severity: 'HIGH' }],
    });
    setDrawerOpen(true);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Customer 360 Risk Profile: ${customerData.name}`}
        description={`Customer ID: ${id} • Identity match gauge, 365-day time-travel slider & family account linkage.`}
        breadcrumbs={['SentinelX', 'Customers', id]}
        action={
          <Link href="/customers">
            <Button variant="outline" size="sm" leftIcon={<ArrowLeft className="h-3.5 w-3.5" />}>
              Back to Directory
            </Button>
          </Link>
        }
      />

      {/* Header Profile Card */}
      <Card>
        <CardContent className="p-6">
          <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6 border-b border-slate-200 dark:border-slate-800 pb-6">
            <div className="flex items-start gap-4">
              <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-indigo-600 font-bold text-white text-xl shadow-md">
                {customerData.name.split(' ').map((n) => n[0]).join('')}
              </div>
              <div>
                <div className="flex items-center gap-3">
                  <h2 className="text-xl font-bold text-slate-900 dark:text-slate-100">{customerData.name}</h2>
                  <Badge variant="danger" className="font-mono">
                    HIGH RISK TIER ({customerData.riskScore}/100)
                  </Badge>
                  <Badge variant="success">{customerData.kycStatus}</Badge>
                </div>
                <div className="mt-1 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-500 dark:text-slate-400 font-mono">
                  <span>ID: {customerData.id}</span>
                  <span>•</span>
                  <span>Email: {customerData.email}</span>
                  <span>•</span>
                  <span>Phone: {customerData.phone}</span>
                </div>
              </div>
            </div>

            {/* Analyst Action Buttons */}
            <div className="flex flex-wrap items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                className="border-red-300 text-red-700 hover:bg-red-50 dark:border-red-800 dark:text-red-400 dark:hover:bg-red-950/40 text-xs"
                onClick={() => handleAction('Freeze Wallet Account')}
              >
                <Ban className="h-3.5 w-3.5 mr-1" /> Freeze Account
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="border-amber-300 text-amber-700 hover:bg-amber-50 dark:border-amber-800 dark:text-amber-400 dark:hover:bg-amber-950/40 text-xs"
                onClick={() => handleAction('Flag as Fraud Mule')}
              >
                <ShieldAlert className="h-3.5 w-3.5 mr-1" /> Flag Fraud Mule
              </Button>
              <Link href={`/network?entity=${id}`}>
                <Button variant="primary" size="sm" className="text-xs" leftIcon={<Share2 className="h-3.5 w-3.5" />}>
                  Explore Graph
                </Button>
              </Link>
            </div>
          </div>

          {/* Time Travel Slider Control */}
          <div className="pt-4 space-y-2 text-xs">
            <div className="flex justify-between font-mono font-semibold">
              <span className="flex items-center gap-1.5 text-indigo-600 dark:text-indigo-400">
                <History className="h-4 w-4" /> 365-Day Identity Time-Travel Inspection
              </span>
              <span className="text-slate-700 dark:text-slate-300">
                {timeTravelDays === 0 ? 'Current Live State' : `${timeTravelDays} Days Ago`}
              </span>
            </div>
            <input
              type="range"
              min="0"
              max="365"
              value={timeTravelDays}
              onChange={(e) => setTimeTravelDays(Number(e.target.value))}
              className="w-full h-2 bg-slate-200 dark:bg-slate-700 rounded-lg appearance-none cursor-pointer accent-indigo-600"
            />
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs
        activeTab={activeTab}
        onChange={setActiveTab}
        tabs={[
          { id: 'overview', label: 'Risk Overview & Signals' },
          { id: 'family', label: `Family & Shared Accounts (${familyMembers.length})` },
          { id: 'transactions', label: `Transaction Stream (${sampleTransactions.length})` },
        ]}
      />

      {activeTab === 'overview' && (
        <div className="space-y-6">
          <RiskScoreBreakdown overallScore={customerData.riskScore} decision={customerData.riskDecision} />
        </div>
      )}

      {activeTab === 'family' && (
        <Card>
          <CardHeader>
            <CardTitle className="text-sm font-bold flex items-center gap-2">
              <Users className="h-4 w-4 text-indigo-500" /> Family & Shared Account Matrix
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-xs">
            {familyMembers.map((m) => (
              <div key={m.name} className="flex items-center justify-between p-3 rounded-lg border border-slate-200 dark:border-slate-800">
                <div>
                  <span className="font-semibold text-slate-900 dark:text-slate-100 block">{m.name} ({m.relation})</span>
                  <span className="font-mono text-slate-500">{m.accountNo}</span>
                </div>
                <Badge variant={m.riskScore >= 70 ? 'warning' : 'success'}>Risk Score: {m.riskScore}/100</Badge>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {/* Drawer */}
      <TransactionDrawer isOpen={drawerOpen} onClose={() => setDrawerOpen(false)} transaction={selectedTx} />
    </div>
  );
}
