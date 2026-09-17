'use client';

import React, { useEffect, useState, use } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { ArrowLeft, ShieldCheck, AlertTriangle, ExternalLink } from 'lucide-react';
import Link from 'next/link';
import { fetchFraudLogById, fetchFraudSignals } from '@/lib/api';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function FraudLogDetailPage({ params }: PageProps) {
  const { id } = use(params);
  const [loading, setLoading] = useState(true);
  const [log, setLog] = useState<any>(null);
  const [signals, setSignals] = useState<any>(null);

  useEffect(() => {
    let mounted = true;
    const loadData = async () => {
      setLoading(true);
      try {
        const [l, s] = await Promise.all([
          fetchFraudLogById(id).catch(() => null),
          fetchFraudSignals(id).catch(() => null),
        ]);
        if (mounted) {
          setLog(l);
          setSignals(s);
        }
      } catch (err) {
        // Fallback
      } finally {
        if (mounted) setLoading(false);
      }
    };
    loadData();
    return () => {
      mounted = false;
    };
  }, [id]);

  const fLog = log || {
    paymentId: id,
    riskScore: 920,
    decision: 'FLAGGED_FOR_INVESTIGATION',
    ruleTriggers: ['VELOCITY_SPIKE_1M', 'MULE_RING_NODE', 'NEW_DEVICE'],
    timestamp: new Date().toISOString(),
    evaluationTimeMs: 7.8,
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Fraud Audit Log: ${id}`}
        description="Detailed record of fraud rules evaluated, ML inference probability, and feature signals."
        breadcrumbs={['SentinelX', 'Fraud Intelligence', id]}
        action={
          <Link href="/fraud">
            <Button variant="outline" size="sm" leftIcon={<ArrowLeft className="h-3.5 w-3.5" />}>
              Back to Fraud Hub
            </Button>
          </Link>
        }
      />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Evaluation Decision Metadata</CardTitle>
            <CardDescription>Gateway execution result for payment {id}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3 text-xs">
            <div className="flex justify-between p-2.5 rounded bg-slate-50 border border-slate-200">
              <span className="font-semibold text-slate-700">Payment ID:</span>
              <code className="font-mono text-blue-700 font-bold">{fLog.paymentId}</code>
            </div>
            <div className="flex justify-between p-2.5 rounded bg-slate-50 border border-slate-200">
              <span className="font-semibold text-slate-700">Risk Index:</span>
              <Badge variant="critical" size="sm" className="font-mono">{fLog.riskScore} / 1000</Badge>
            </div>
            <div className="flex justify-between p-2.5 rounded bg-slate-50 border border-slate-200">
              <span className="font-semibold text-slate-700">System Decision:</span>
              <Badge variant="warning" size="sm">{fLog.decision}</Badge>
            </div>
            <div className="flex justify-between p-2.5 rounded bg-slate-50 border border-slate-200">
              <span className="font-semibold text-slate-700">Pipeline Latency:</span>
              <span className="font-mono text-slate-800">{fLog.evaluationTimeMs} ms</span>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <AlertTriangle className="h-4 w-4 text-rose-600" /> Triggered Rule Signals
            </CardTitle>
            <CardDescription>Rules that matched during gateway evaluation</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-xs">
            {fLog.ruleTriggers.map((trig: string) => (
              <div key={trig} className="flex items-center justify-between p-3 rounded border border-rose-200 bg-rose-50 text-rose-900 font-mono">
                <span>{trig}</span>
                <Badge variant="critical" size="sm">FLAGGED</Badge>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
