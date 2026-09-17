'use client';

import React from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { FileText, Download } from 'lucide-react';

export default function ReportsPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Regulatory Reports & Audit Export Center"
        description="Generate SAR compliance reports, settlement summaries, and executive PDF audits."
        breadcrumbs={['SentinelX', 'Operations', 'Reports']}
      />

      <Card>
        <CardHeader>
          <CardTitle>Generated Compliance Reports</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3 text-xs">
          {[
            { name: 'Monthly_FIU_SAR_Export_Sept_2026.pdf', type: 'SAR COMPLIANCE', date: 'Sept 2026' },
            { name: 'Settlement_Reconciliation_Summary_Batch_9912.pdf', type: 'RECONCILIATION', date: 'Sept 2026' },
          ].map((r) => (
            <div key={r.name} className="p-3 rounded border border-slate-200 bg-slate-50 flex items-center justify-between">
              <div>
                <span className="font-bold text-slate-800 block">{r.name}</span>
                <span className="text-[10px] text-slate-500">{r.type} • {r.date}</span>
              </div>
              <Button variant="outline" size="xs" rightIcon={<Download className="h-3 w-3" />}>
                Download Export
              </Button>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
