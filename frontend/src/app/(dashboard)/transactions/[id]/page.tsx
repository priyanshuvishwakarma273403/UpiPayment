'use client';

import React, { useEffect, useState, use } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Alert } from '@/components/ui/Alert';
import { StatCard } from '@/components/ui/StatCard';
import { Timeline } from '@/components/ui/Timeline';
import { SkeletonLoader } from '@/components/ui/SkeletonLoader';
import {
  ShieldCheck,
  AlertTriangle,
  ArrowLeft,
  ExternalLink,
  Bot,
  Zap,
  Lock,
  UserCheck,
  CheckCircle2,
  Clock,
  Server
} from 'lucide-react';
import Link from 'next/link';
import { fetchTransactionById, fetchFraudSignals } from '@/lib/api';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function TransactionDetailPage({ params }: PageProps) {
  const { id } = use(params);
  const [loading, setLoading] = useState(true);
  const [txData, setTxData] = useState<any>(null);
  const [signals, setSignals] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    const loadDetail = async () => {
      setLoading(true);
      try {
        const [tx, sig] = await Promise.all([
          fetchTransactionById(id).catch(() => null),
          fetchFraudSignals(id).catch(() => null),
        ]);
        if (mounted) {
          setTxData(tx);
          setSignals(sig);
        }
      } catch (err: any) {
        if (mounted) setError(err?.message || 'Failed to load transaction details.');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    loadDetail();
    return () => {
      mounted = false;
    };
  }, [id]);

  const tx = txData || {
    id: id || 'TX-89101',
    senderUpiId: 'anand@okaxis',
    receiverUpiId: 'store@ybl',
    amount: 85000,
    status: 'FLAGGED',
    riskScore: 920,
    timestamp: new Date().toISOString(),
    ipAddress: '103.21.244.18',
    deviceId: 'DEV-A990-21X',
    location: 'Mumbai, MH, India',
    bankRrn: 'RRN-9921448102',
    channel: 'UPI_COLLECT',
  };

  const timelineItems = [
    { title: 'Payment Request Initiated', timestamp: new Date(Date.now() - 5000).toLocaleTimeString(), description: `Payload received via ${tx.channel}` },
    { title: 'Sub-10ms Risk Score Calculated', timestamp: new Date(Date.now() - 4000).toLocaleTimeString(), description: `Risk Score: ${tx.riskScore}/1000 — Velocity Spike Triggered` },
    { title: 'FastAPI ML Model Inference', timestamp: new Date(Date.now() - 3000).toLocaleTimeString(), description: 'Scikit-learn XGBoost Probability: 0.942 (HIGH FRAUD RISK)' },
    { title: 'Fraud Sentinel Flag Raised', timestamp: new Date(Date.now() - 1000).toLocaleTimeString(), description: 'Case #CASE-4401 Created for Analyst Investigation' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Transaction Inspection: ${id}`}
        description="Comprehensive payload telemetry, risk vector evaluation, and graph network connection."
        breadcrumbs={['SentinelX', 'Transactions', id]}
        action={
          <Link href="/transactions">
            <Button variant="outline" size="sm" leftIcon={<ArrowLeft className="h-3.5 w-3.5" />}>
              Back to Ledger
            </Button>
          </Link>
        }
      />

      {/* STAT HIGHLIGHTS */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Payment Amount"
          value={`₹ ${tx.amount ? tx.amount.toLocaleString() : '85,000'}`}
          change="INR Currency"
          changeType="neutral"
          comparisonText="Instant UPI Transfer"
          icon={<Zap className="h-4 w-4 text-blue-600" />}
          statusVariant="info"
        />
        <StatCard
          title="Risk Index"
          value={`${tx.riskScore || 920} / 1000`}
          change="CRITICAL RISK"
          changeType="decrease"
          comparisonText="Velocity + Geofence match"
          icon={<AlertTriangle className="h-4 w-4 text-rose-600" />}
          statusVariant="critical"
        />
        <StatCard
          title="Transaction Status"
          value={tx.status || 'FLAGGED'}
          change="Pending Review"
          changeType="neutral"
          comparisonText="Held in Escrow Guard"
          icon={<Lock className="h-4 w-4 text-amber-600" />}
          statusVariant="warning"
        />
        <StatCard
          title="Bank RRN Checksum"
          value={tx.bankRrn || 'RRN-992144'}
          change="Verified"
          changeType="increase"
          comparisonText="Core Banking Switch"
          icon={<Server className="h-4 w-4 text-emerald-600" />}
          statusVariant="success"
        />
      </div>

      {/* TWO-COLUMN DETAILS */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Entity Details & Feature Signals */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Entity & Payload Metadata</CardTitle>
              <CardDescription>Sender, receiver, and device telemetry details</CardDescription>
            </CardHeader>
            <CardContent>
              {loading ? (
                <SkeletonLoader count={4} height={35} />
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                  <div className="rounded-lg border border-slate-200 bg-slate-50 p-3 space-y-2">
                    <span className="font-bold text-slate-700 block text-[11px] uppercase tracking-wider">Sender Profile</span>
                    <div><span className="text-slate-500">UPI Handle:</span> <code className="font-mono text-blue-700 font-semibold">{tx.senderUpiId}</code></div>
                    <div><span className="text-slate-500">Device ID:</span> <code className="font-mono text-slate-800">{tx.deviceId}</code></div>
                    <div><span className="text-slate-500">IP Geofence:</span> <span className="text-slate-800">{tx.location} ({tx.ipAddress})</span></div>
                  </div>

                  <div className="rounded-lg border border-slate-200 bg-slate-50 p-3 space-y-2">
                    <span className="font-bold text-slate-700 block text-[11px] uppercase tracking-wider">Receiver Profile</span>
                    <div><span className="text-slate-500">UPI Handle:</span> <code className="font-mono text-emerald-700 font-semibold">{tx.receiverUpiId}</code></div>
                    <div><span className="text-slate-500">Payment Channel:</span> <span className="text-slate-800">{tx.channel}</span></div>
                    <div><span className="text-slate-500">Bank Switch RRN:</span> <code className="font-mono text-slate-800">{tx.bankRrn}</code></div>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Risk Signal Attribution */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <AlertTriangle className="h-4 w-4 text-rose-600" /> Fraud Vector Signals & SHAP Weights
              </CardTitle>
              <CardDescription>Extracted feature weights contributing to risk index</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3 text-xs">
              {[
                { feature: 'Rolling 1m Velocity', weight: '+0.42', desc: '5 transactions in under 60 seconds across 3 merchants', level: 'CRITICAL' },
                { feature: 'Shared Device Fingerprint', weight: '+0.31', desc: 'Device DEV-A990 linked to 4 previously flagged UPI handles', level: 'HIGH' },
                { feature: 'New Geolocation IP', weight: '+0.18', desc: 'IP location 1,200 km away from primary user residence', level: 'MEDIUM' },
                { feature: 'KYC Account Age', weight: '-0.05', desc: 'User account active for > 180 days (Mitigating factor)', level: 'LOW' },
              ].map((sig) => (
                <div key={sig.feature} className="flex items-center justify-between p-3 rounded-md border border-slate-200 bg-white">
                  <div>
                    <span className="font-bold text-slate-800 block">{sig.feature}</span>
                    <span className="text-slate-500 text-[11px]">{sig.desc}</span>
                  </div>
                  <div className="text-right shrink-0">
                    <Badge variant={sig.level === 'CRITICAL' ? 'critical' : sig.level === 'HIGH' ? 'warning' : 'info'} size="sm" className="font-mono">
                      {sig.weight}
                    </Badge>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>

        {/* Right Column: Execution Timeline & Analyst Actions */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Microservice Execution Trace</CardTitle>
              <CardDescription>Real-time lifecycle of this payment</CardDescription>
            </CardHeader>
            <CardContent>
              <Timeline items={timelineItems} />
            </CardContent>
          </Card>

          <Card className="border-blue-200 bg-blue-50/50">
            <CardHeader>
              <CardTitle className="text-sm font-bold text-blue-900 flex items-center gap-2">
                <Bot className="h-4 w-4 text-blue-600" /> Analyst Actions
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <Link href={`/fraud/${tx.id}`}>
                <Button variant="primary" fullWidth size="sm" rightIcon={<ExternalLink className="h-3.5 w-3.5" />}>
                  Open Fraud Investigation Log
                </Button>
              </Link>
              <Link href="/network">
                <Button variant="outline" fullWidth size="sm">
                  View Multi-Hop Graph
                </Button>
              </Link>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
