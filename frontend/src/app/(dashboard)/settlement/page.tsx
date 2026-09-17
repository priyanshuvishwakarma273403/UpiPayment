'use client';

import React from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { DataTable } from '@/components/ui/DataTable';
import { Building2, CreditCard } from 'lucide-react';

export default function SettlementPage() {
  const settlements = [
    { id: 'SETT-9901', sponsorBank: 'HDFC Bank', merchant: 'Global Store Ltd', netAmount: 1480000, status: 'RECONCILED' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Sponsor Banking & Settlement Reconciliation"
        description="Monitor automated settlement payouts, sponsor bank ledgers, and reserve holdback pools."
        breadcrumbs={['SentinelX', 'Operations', 'Settlement']}
      />

      <Card>
        <CardHeader>
          <CardTitle className="text-sm font-bold">Daily Settlement Ledger</CardTitle>
        </CardHeader>
        <CardContent>
          <DataTable
            data={settlements}
            pagination={false}
            columns={[
              { header: 'Settlement ID', accessor: (r: any) => <span className="font-mono text-xs font-bold text-indigo-600 dark:text-indigo-400">{r.id}</span> },
              { header: 'Sponsor Bank', accessor: (r: any) => <span className="font-semibold text-xs text-slate-900 dark:text-slate-100">{r.sponsorBank}</span> },
              { header: 'Net Settlement', accessor: (r: any) => <span className="font-mono text-xs font-bold">₹{r.netAmount.toLocaleString('en-IN')}</span> },
              { header: 'Reconciliation', accessor: (r: any) => <Badge variant="success">{r.status}</Badge> },
            ]}
          />
        </CardContent>
      </Card>
    </div>
  );
}
