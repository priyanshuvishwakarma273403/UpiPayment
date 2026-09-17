'use client';

import React, { useEffect, useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { DataTable } from '@/components/ui/DataTable';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { SkeletonLoader } from '@/components/ui/SkeletonLoader';
import { Search, ArrowUpRight, Filter, ShieldCheck, UserCheck } from 'lucide-react';
import Link from 'next/link';
import { fetchFraudCases } from '@/lib/api';

export default function CasesPage() {
  const [loading, setLoading] = useState(true);
  const [cases, setCases] = useState<any[]>([]);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  useEffect(() => {
    const loadCases = async () => {
      setLoading(true);
      try {
        const res = await fetchFraudCases();
        setCases(Array.isArray(res) ? res : []);
      } catch (err) {
        setCases([]);
      } finally {
        setLoading(false);
      }
    };
    loadCases();
  }, []);

  const columns = [
    {
      header: 'Case ID',
      accessor: (r: any) => (
        <Link href={`/cases/${r.id || r.caseId || 'CASE-101'}`} className="font-mono text-xs font-bold text-blue-600 hover:underline">
          {r.id || r.caseId || 'CASE-101'}
        </Link>
      ),
    },
    { header: 'Target Payment', accessor: (r: any) => <code className="font-mono text-xs text-slate-700">{r.paymentId || 'PAY-88231'}</code> },
    { header: 'Risk Severity', accessor: (r: any) => <Badge variant={r.severity === 'CRITICAL' ? 'critical' : 'warning'} size="sm">{r.severity || 'HIGH'}</Badge> },
    { header: 'Assigned Analyst', accessor: (r: any) => <span className="text-xs font-semibold text-slate-800">{r.assignee || 'Vikram Singh'}</span> },
    { header: 'Case Status', accessor: (r: any) => <Badge variant="info" size="sm">{r.status || 'OPEN'}</Badge> },
    {
      header: 'Action',
      accessor: (r: any) => (
        <Link href={`/cases/${r.id || r.caseId || 'CASE-101'}`}>
          <Button variant="outline" size="xs" rightIcon={<ArrowUpRight className="h-3 w-3" />}>
            Open Case
          </Button>
        </Link>
      ),
    },
  ];

  const defaultCases = [
    { id: 'CASE-4401', paymentId: 'PAY-99821', severity: 'CRITICAL', assignee: 'Vikram Singh (You)', status: 'INVESTIGATING' },
    { id: 'CASE-4400', paymentId: 'PAY-99818', severity: 'HIGH', assignee: 'Meera Nair', status: 'OPEN' },
    { id: 'CASE-4399', paymentId: 'PAY-99804', severity: 'MEDIUM', assignee: 'Anand Sharma', status: 'CONFIRMED_FRAUD' },
  ];

  const dataToRender = (cases.length > 0 ? cases : defaultCases).filter((c) => {
    const matchesSearch = !search || (c.id || '').toLowerCase().includes(search.toLowerCase()) || (c.paymentId || '').toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || c.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Fraud Investigation Cases"
        description="Active analyst investigation queue, evidence gathering, and status lifecycle."
        breadcrumbs={['SentinelX', 'Operations', 'Cases']}
      />

      <Card>
        <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <CardTitle>Investigation Case Queue</CardTitle>
            <CardDescription>Filter cases by status or assignee</CardDescription>
          </div>
          <div className="flex items-center gap-3">
            <div className="w-56">
              <Input
                placeholder="Search Case ID..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                leftIcon={<Search className="h-4 w-4 text-slate-400" />}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <SkeletonLoader count={4} height={40} />
          ) : (
            <DataTable data={dataToRender} columns={columns} pageSize={10} />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
