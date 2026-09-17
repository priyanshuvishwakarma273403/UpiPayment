'use client';

import React from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Bell, AlertTriangle, ShieldCheck, CheckCircle2 } from 'lucide-react';

export default function NotificationsPage() {
  const alerts = [
    { id: 1, title: 'Critical Risk Alert: Payment PAY-99821', time: '10m ago', type: 'CRITICAL', text: 'Velocity burst (5 tx / 1m) detected for sender anand@okaxis.' },
    { id: 2, title: 'AML Watchlist Match: Vikram Malhotra', time: '1h ago', type: 'WARNING', text: 'Fuzzy match 94.2% against OFAC SDN list.' },
    { id: 3, title: 'ML Retraining Completed', time: '3h ago', type: 'INFO', text: 'Candidate model v2.4.1 trained on 1,420 labels.' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="System Notifications & Operational Alerts"
        description="Real-time stream of high-risk flags, AML sanction updates, and ML pipeline events."
        breadcrumbs={['SentinelX', 'Operations', 'Notifications']}
      />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Bell className="h-4 w-4 text-blue-600" /> Live Alert Ledger
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {alerts.map((a) => (
            <div key={a.id} className="p-4 rounded-lg border border-slate-200 bg-white flex items-start justify-between text-xs">
              <div className="space-y-1">
                <div className="font-bold text-slate-900 flex items-center gap-2">
                  <span>{a.title}</span>
                  <Badge variant={a.type === 'CRITICAL' ? 'critical' : a.type === 'WARNING' ? 'warning' : 'info'} size="sm">
                    {a.type}
                  </Badge>
                </div>
                <p className="text-slate-600">{a.text}</p>
              </div>
              <span className="text-[10px] text-slate-400 font-mono shrink-0">{a.time}</span>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
