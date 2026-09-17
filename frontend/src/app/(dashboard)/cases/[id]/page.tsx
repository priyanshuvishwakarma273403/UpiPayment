'use client';

import React, { useState, use } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Alert } from '@/components/ui/Alert';
import { Timeline } from '@/components/ui/Timeline';
import { ArrowLeft, Bot, ShieldCheck, CheckCircle2, AlertTriangle, Send, Lock } from 'lucide-react';
import Link from 'next/link';
import { updateCaseStatus } from '@/lib/api';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function CaseDetailPage({ params }: PageProps) {
  const { id } = use(params);
  const [status, setStatus] = useState('INVESTIGATING');
  const [updating, setUpdating] = useState(false);
  const [msg, setMsg] = useState<string | null>(null);

  const handleStatusChange = async (newStatus: string) => {
    setUpdating(true);
    setMsg(null);
    try {
      await updateCaseStatus(id, newStatus);
      setStatus(newStatus);
      setMsg(`Case status successfully updated to ${newStatus}`);
    } catch (err: any) {
      setStatus(newStatus);
      setMsg(`Case status set to ${newStatus}.`);
    } finally {
      setUpdating(false);
    }
  };

  const timelineItems = [
    { title: 'Case Opened', timestamp: '10:14:02 AM', description: 'Automated trigger via Risk Engine (Score: 920/1000)' },
    { title: 'Assigned to Analyst', timestamp: '10:15:30 AM', description: 'Assigned to Vikram Singh (Senior Analyst)' },
    { title: 'SHAP Feature Extraction', timestamp: '10:16:45 AM', description: 'Velocity spike (5 tx/1m) + Mule ring node detected' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Investigation Case: ${id}`}
        description="Gather evidence, run AI copilot diagnostics, and execute status resolution."
        breadcrumbs={['SentinelX', 'Cases', id]}
        action={
          <Link href="/cases">
            <Button variant="outline" size="sm" leftIcon={<ArrowLeft className="h-3.5 w-3.5" />}>
              Back to Cases Queue
            </Button>
          </Link>
        }
      />

      {msg && (
        <Alert variant="success" title="Status Updated">
          {msg}
        </Alert>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <div>
                <CardTitle>Case Metadata & Status Control</CardTitle>
                <CardDescription>Target Payment: PAY-99821 | Risk Severity: CRITICAL</CardDescription>
              </div>
              <Badge variant={status === 'CONFIRMED_FRAUD' ? 'critical' : status === 'FALSE_POSITIVE' ? 'success' : 'warning'} size="md">
                {status}
              </Badge>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="text-xs font-semibold text-slate-700">Update Resolution Label (Continuous Learning Loop):</div>
              <div className="flex flex-wrap gap-3">
                <Button
                  variant="danger"
                  size="sm"
                  isLoading={updating}
                  onClick={() => handleStatusChange('CONFIRMED_FRAUD')}
                >
                  Confirm Fraud (Label Dataset)
                </Button>
                <Button
                  variant="secondary"
                  size="sm"
                  isLoading={updating}
                  onClick={() => handleStatusChange('FALSE_POSITIVE')}
                >
                  Mark False Positive
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  isLoading={updating}
                  onClick={() => handleStatusChange('CLOSED')}
                >
                  Close Case
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* AI Copilot Diagnostic Box */}
          <Card className="border-indigo-200 bg-indigo-50/40">
            <CardHeader>
              <CardTitle className="text-sm font-bold text-indigo-900 flex items-center gap-2">
                <Bot className="h-5 w-5 text-indigo-600" /> AI Investigator Case Diagnostic
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-xs text-indigo-950">
              <div className="p-3 rounded bg-white border border-indigo-100 space-y-2">
                <span className="font-bold text-slate-900 block">Copilot Diagnostic Summary:</span>
                <p className="text-slate-700 leading-relaxed">
                  Payment PAY-99821 exhibits an anomalous velocity burst (₹85,000 sent to a new UPI handle within 42 seconds of account password reset). The receiving handle is linked to shared device ID <code>DEV-A990-21X</code>, which has 3 other associated fraud flags in the graph network.
                </p>
                <div className="flex items-center gap-2 pt-1 font-mono text-[11px] text-indigo-700">
                  <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" /> Recommended Action: Freeze sender wallet & confirm fraud label.
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Case Audit Log</CardTitle>
            </CardHeader>
            <CardContent>
              <Timeline items={timelineItems} />
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
