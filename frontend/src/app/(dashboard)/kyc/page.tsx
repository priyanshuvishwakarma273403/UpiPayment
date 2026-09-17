'use client';

import React from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { DataTable } from '@/components/ui/DataTable';
import { UserCheck, ShieldCheck, FileText, CheckCircle2, RefreshCw } from 'lucide-react';

export default function KycPage() {
  const kycRecords = [
    { id: 'KYC-1092', name: 'Anand Kumar Sharma', pan: 'ABCDE1234F', aadhaar: '•••• 8912', status: 'FULL_KYC_VERIFIED', score: '100%' },
    { id: 'KYC-1088', name: 'Priya Verma', pan: 'PQRSW9876K', aadhaar: '•••• 4410', status: 'FULL_KYC_VERIFIED', score: '100%' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Global Identity & KYC Verification Hub"
        description="Automated PAN, Aadhaar, Passport, and Video-KYC verification pipeline connected to KYC Microservice."
        breadcrumbs={['SentinelX', 'Operations', 'KYC Hub']}
      />

      <Card>
        <CardHeader>
          <CardTitle className="text-sm font-bold">Verified Customer KYC Queue</CardTitle>
        </CardHeader>
        <CardContent>
          <DataTable
            data={kycRecords}
            pagination={false}
            columns={[
              { header: 'KYC ID', accessor: (r: any) => <span className="font-mono text-xs font-bold text-indigo-600 dark:text-indigo-400">{r.id}</span> },
              { header: 'Customer Name', accessor: (r: any) => <span className="font-semibold text-xs text-slate-900 dark:text-slate-100">{r.name}</span> },
              { header: 'PAN Document', accessor: (r: any) => <span className="font-mono text-xs text-slate-600 dark:text-slate-400">{r.pan}</span> },
              { header: 'Verification Status', accessor: (r: any) => <Badge variant="success">{r.status}</Badge> },
            ]}
          />
        </CardContent>
      </Card>
    </div>
  );
}
