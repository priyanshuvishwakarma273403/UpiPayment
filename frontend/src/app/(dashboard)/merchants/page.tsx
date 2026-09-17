'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';
import { DataTable } from '@/components/ui/DataTable';
import { Building, QrCode, ArrowUpRight, Search } from 'lucide-react';
import Link from 'next/link';

export default function MerchantsPage() {
  const [search, setSearch] = useState('');

  const columns = [
    {
      header: 'Merchant ID',
      accessor: (r: any) => (
        <Link href={`/merchants/${r.id}`} className="font-mono text-xs font-bold text-blue-600 hover:underline">
          {r.id}
        </Link>
      ),
    },
    { header: 'Business Name', accessor: (r: any) => <span className="text-xs font-semibold text-slate-900">{r.name}</span> },
    { header: 'Merchant UPI ID', accessor: (r: any) => <code className="font-mono text-xs text-slate-700">{r.upiId}</code> },
    { header: 'Category Code (MCC)', accessor: (r: any) => <span className="font-mono text-xs text-slate-600">{r.mcc}</span> },
    { header: 'Settlement Tier', accessor: (r: any) => <Badge variant="success" size="sm">{r.tier}</Badge> },
    {
      header: 'Action',
      accessor: (r: any) => (
        <Link href={`/merchants/${r.id}`}>
          <Button variant="outline" size="xs" rightIcon={<ArrowUpRight className="h-3 w-3" />}>
            Manage QR / Ledger
          </Button>
        </Link>
      ),
    },
  ];

  const merchants = [
    { id: 'MCH-8001', name: 'Apex Electronics Retail', upiId: 'apex@ybl', mcc: '5732', tier: 'SAME_DAY_T+0' },
    { id: 'MCH-8002', name: 'Metro Supermarket', upiId: 'metro@okaxis', mcc: '5411', tier: 'T+1' },
    { id: 'MCH-8003', name: 'Starlight Dining', upiId: 'starlight@hdfc', mcc: '5812', tier: 'T+1' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Merchant Risk & Settlement Portal"
        description="Merchant onboarding, dynamic payload QR generation, and settlement ledger inspection."
        breadcrumbs={['SentinelX', 'Operations', 'Merchants']}
      />

      <Card>
        <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <CardTitle>Merchant Accounts Directory</CardTitle>
            <CardDescription>Search by Merchant ID, Business Name, or MCC code</CardDescription>
          </div>
          <div className="w-64">
            <Input
              placeholder="Search Merchants..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              leftIcon={<Search className="h-4 w-4 text-slate-400" />}
            />
          </div>
        </CardHeader>
        <CardContent>
          <DataTable data={merchants} columns={columns} pageSize={10} />
        </CardContent>
      </Card>
    </div>
  );
}
