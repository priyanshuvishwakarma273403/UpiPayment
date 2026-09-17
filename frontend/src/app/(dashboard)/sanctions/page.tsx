'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';
import { DataTable } from '@/components/ui/DataTable';
import { Search, ShieldCheck, UserCheck, AlertTriangle, ExternalLink, RefreshCw, FileText } from 'lucide-react';
import { useToast } from '@/lib/useToast';

interface SanctionMatch {
  id: string;
  name: string;
  matchedTerm: string;
  fuzzyMatchScore: number; // e.g. 96%
  watchlist: 'OFAC_SDN' | 'UN_SECURITY_COUNCIL' | 'EU_SANCTIONS' | 'MHA_INDIA';
  category: 'INDIVIDUAL' | 'ENTITY' | 'VESSEL';
  country: string;
  status: 'HIGH_CONFIDENCE_MATCH' | 'POTENTIAL_MATCH' | 'CLEARED';
}

export default function SanctionsPage() {
  const { toast } = useToast();
  const [query, setQuery] = useState('Anand Sharma');
  const [loading, setLoading] = useState(false);

  const sampleMatches: SanctionMatch[] = [
    { id: 'SAN-9901', name: 'Anant Kumar Sarma', matchedTerm: 'Anand Sharma', fuzzyMatchScore: 94, watchlist: 'OFAC_SDN', category: 'INDIVIDUAL', country: 'IN / RU', status: 'POTENTIAL_MATCH' },
    { id: 'SAN-8812', name: 'Sharma Enterprises Ltd', matchedTerm: 'Anand Sharma', fuzzyMatchScore: 82, watchlist: 'EU_SANCTIONS', category: 'ENTITY', country: 'IN / CY', status: 'POTENTIAL_MATCH' },
  ];

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      toast('Fuzzy Sanction Screening Complete', `Scanned OFAC, UN & EU databases for "${query}".`, 'info');
    }, 600);
  };

  const columns = [
    {
      header: 'Sanction ID',
      accessor: (r: SanctionMatch) => <span className="font-mono text-xs font-bold text-indigo-600 dark:text-indigo-400">{r.id}</span>,
    },
    {
      header: 'Matched Entity Name',
      accessor: (r: SanctionMatch) => (
        <div>
          <span className="text-xs font-semibold text-slate-900 dark:text-slate-100 block">{r.name}</span>
          <span className="text-[10px] text-slate-500 font-mono">Matched against: "{r.matchedTerm}"</span>
        </div>
      ),
    },
    {
      header: 'Fuzzy Match %',
      accessor: (r: SanctionMatch) => (
        <Badge variant={r.fuzzyMatchScore >= 90 ? 'danger' : 'warning'} className="font-mono">
          {r.fuzzyMatchScore}% MATCH
        </Badge>
      ),
    },
    {
      header: 'Watchlist Source',
      accessor: (r: SanctionMatch) => <Badge variant="info" size="sm">{r.watchlist}</Badge>,
    },
    {
      header: 'Jurisdiction',
      accessor: (r: SanctionMatch) => <span className="font-mono text-xs text-slate-600 dark:text-slate-400">{r.country}</span>,
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Global Sanctions, PEP & AML Watchlist Screening"
        description="Sub-10ms Levenshtein & Jaro-Winkler fuzzy matching across OFAC SDN, UN, EU, and MHA Sanction databases."
        breadcrumbs={['SentinelX', 'AML Operations', 'Sanctions Screening']}
        action={
          <Button variant="outline" size="sm" onClick={handleSearch} isLoading={loading} leftIcon={<RefreshCw className="h-3.5 w-3.5" />}>
            Re-index Watchlists
          </Button>
        }
      />

      <Card>
        <CardHeader>
          <CardTitle className="text-sm font-bold">Interactive Watchlist Search Studio</CardTitle>
          <CardDescription>Enter name, UPI handle, passport number, or registration ID</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <form onSubmit={handleSearch} className="flex gap-3">
            <div className="flex-1">
              <Input
                placeholder="Search Entity Name (e.g., 'Anand Sharma' or 'Global Trading Corp')..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                leftIcon={<Search className="h-4 w-4 text-slate-400" />}
              />
            </div>
            <Button type="submit" variant="primary" isLoading={loading} rightIcon={<Search className="h-4 w-4" />}>
              Run Fuzzy Match
            </Button>
          </form>

          {/* Results Table */}
          <DataTable data={sampleMatches} columns={columns} pagination={false} />
        </CardContent>
      </Card>
    </div>
  );
}
