'use client';

import React from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Lock, ShieldCheck, Activity, Terminal } from 'lucide-react';

export default function AuditPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Observability & Security Audit Trail"
        description="Immutable security log, OpenTelemetry traces, Prometheus metrics, and JWT session audit."
        breadcrumbs={['SentinelX', 'Operations', 'Audit & Security']}
      />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Lock className="h-4 w-4 text-emerald-600" /> Security & Auth Log Trail
            </CardTitle>
            <CardDescription>JWT token issuance, login attempts, and role permission audits</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-xs font-mono">
            {[
              { event: 'JWT_LOGIN_SUCCESS', user: 'vikram@upimesh', ip: '103.21.244.18', time: '10:12:00 AM' },
              { event: 'ROLE_GUARD_PASS', user: 'vikram@upimesh', role: 'ROLE_INVESTIGATOR', time: '10:12:05 AM' },
              { event: 'CASE_STATUS_UPDATE', user: 'vikram@upimesh', case: 'CASE-4401', time: '10:18:22 AM' },
            ].map((e, idx) => (
              <div key={idx} className="p-2.5 rounded bg-slate-900 text-slate-200 flex justify-between">
                <span><span className="text-emerald-400 font-bold">{e.event}</span> • {e.user}</span>
                <span className="text-slate-400 text-[10px]">{e.time}</span>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-4 w-4 text-purple-600" /> OpenTelemetry Active Traces
            </CardTitle>
            <CardDescription>Jaeger distributed trace spans across microservice mesh</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-xs font-mono">
            {[
              { traceId: 'tr-9921a8b', path: 'Gateway -> Payment -> Risk -> Fraud -> ML', duration: '7.8 ms' },
              { traceId: 'tr-9921a8c', path: 'Gateway -> Auth -> User', duration: '3.2 ms' },
              { traceId: 'tr-9921a8d', path: 'Gateway -> Kyc -> AadhaarCheck', duration: '12.4 ms' },
            ].map((t) => (
              <div key={t.traceId} className="p-2.5 rounded bg-slate-900 text-slate-200 flex justify-between">
                <div>
                  <span className="text-blue-400 font-bold">{t.traceId}</span>
                  <span className="text-slate-400 block text-[10px]">{t.path}</span>
                </div>
                <span className="text-emerald-400 font-bold">{t.duration}</span>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
