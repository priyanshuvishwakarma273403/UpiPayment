'use client';

import React from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { DataTable } from '@/components/ui/DataTable';
import { ShieldAlert, AlertTriangle } from 'lucide-react';

export default function DisputesPage() {
  const disputes = [
    { id: 'DISP-8912', paymentId: 'TX-89101', amount: 85000, reason: 'Unauthorized UPI Charge Claim', status: 'UNDER_REVIEW' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Chargeback & Refund Fraud Dispute Studio"
        description="Monitor merchant dispute claims, policy abuse, and fraudulent chargeback patterns."
        breadcrumbs={['SentinelX', 'Operations', 'Disputes']}
      />

      <Card>
        <CardHeader>
          <CardTitle className="text-sm font-bold">Dispute Resolution Queue</CardTitle>
        </CardHeader>
        <CardContent>
          <DataTable
            data={disputes}
            pagination={false}
            columns={[
              { header: 'Dispute ID', accessor: (r: any) => <span className="font-mono text-xs font-bold text-indigo-600 dark:text-indigo-400">{r.id}</span> },
              { header: 'Payment ID', accessor: (r: any) => <span className="font-mono text-xs text-slate-700 dark:text-slate-300">{r.paymentId}</span> },
              { header: 'Disputed Amount', accessor: (r: any) => <span className="font-mono text-xs font-bold">₹{r.amount.toLocaleString('en-IN')}</span> },
              { header: 'Status', accessor: (r: any) => <Badge variant="warning">{r.status}</Badge> },
            ]}
          />
        </CardContent>
      </Card>
    </div>
  );
}
