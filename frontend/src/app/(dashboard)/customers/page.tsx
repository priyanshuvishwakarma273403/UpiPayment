'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';
import { DataTable } from '@/components/ui/DataTable';
import { Search, UserCheck, ShieldCheck, ArrowUpRight } from 'lucide-react';
import Link from 'next/link';

export default function CustomersPage() {
  const [search, setSearch] = useState('');

  const columns = [
    {
      header: 'User / Customer ID',
      accessor: (r: any) => (
        <Link href={`/customers/${r.id}`} className="font-mono text-xs font-bold text-blue-600 hover:underline">
          {r.id}
        </Link>
      ),
    },
    { header: 'Full Name', accessor: (r: any) => <span className="text-xs font-semibold text-slate-800">{r.name}</span> },
    { header: 'UPI Handle', accessor: (r: any) => <code className="font-mono text-xs text-slate-700">{r.upiId}</code> },
    { header: 'KYC Status', accessor: (r: any) => <Badge variant={r.kyc === 'FULL_KYC' ? 'success' : 'warning'} size="sm">{r.kyc}</Badge> },
    { header: 'Risk Tier', accessor: (r: any) => <Badge variant={r.riskTier === 'CRITICAL' ? 'critical' : r.riskTier === 'HIGH' ? 'warning' : 'info'} size="sm">{r.riskTier}</Badge> },
    { header: 'Wallet Balance', accessor: (r: any) => <span className="font-mono text-xs font-bold text-slate-900">₹ {r.balance.toLocaleString()}</span> },
    {
      header: 'Action',
      accessor: (r: any) => (
        <Link href={`/customers/${r.id}`}>
          <Button variant="outline" size="xs" rightIcon={<ArrowUpRight className="h-3 w-3" />}>
            Profile
          </Button>
        </Link>
      ),
    },
  ];

  const customers = [
    { id: 'USR-1001', name: 'Anand Sharma', upiId: 'anand@okaxis', kyc: 'FULL_KYC', riskTier: 'CRITICAL', balance: 145000 },
    { id: 'USR-1002', name: 'Priya Verma', upiId: 'priya@ybl', kyc: 'FULL_KYC', riskTier: 'LOW', balance: 24500 },
    { id: 'USR-1003', name: 'Vikram Singh', upiId: 'vikram@okicici', kyc: 'FULL_KYC', riskTier: 'MEDIUM', balance: 88000 },
    { id: 'USR-1004', name: 'Meera Patel', upiId: 'meera@gpay', kyc: 'MIN_KYC', riskTier: 'HIGH', balance: 12000 },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Customer Risk Profiles & KYC Directory"
        description="Search customer accounts, verify KYC completion level, wallet balance, and risk tier."
        breadcrumbs={['SentinelX', 'Operations', 'Customers']}
      />

      <Card>
        <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <CardTitle>Customer Directory</CardTitle>
            <CardDescription>Search by User ID, Name, or UPI Handle</CardDescription>
          </div>
          <div className="w-64">
            <Input
              placeholder="Search Customer..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              leftIcon={<Search className="h-4 w-4 text-slate-400" />}
            />
          </div>
        </CardHeader>
        <CardContent>
          <DataTable data={customers} columns={columns} pageSize={10} />
        </CardContent>
      </Card>
    </div>
  );
}
