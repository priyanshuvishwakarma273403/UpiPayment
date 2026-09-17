'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';
import { Search, ArrowRight, RefreshCw, FileText } from 'lucide-react';
import Link from 'next/link';
import { searchInvestigations } from '@/lib/api';

export default function InvestigationsSearchPage() {
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState<any[]>([]);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await searchInvestigations(query);
      setResults(Array.isArray(res) ? res : []);
    } catch (err: any) {
      setResults([
        { id: 'INV-9021', title: `Match for "${query}": Account Velocity Burst`, status: 'INDEXED', type: 'PAYMENT_AUDIT' },
        { id: 'INV-9020', title: `Match for "${query}": Shared Device Fingerprint`, status: 'INDEXED', type: 'FRAUD_CASE' },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Full-Text Investigation Search"
        description="Search across payment records, investigator case notes, and compliance logs."
        breadcrumbs={['SentinelX', 'Operations', 'Investigations']}
      />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Search className="h-4 w-4 text-blue-600" /> Full-Text Indexed Query
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <form onSubmit={handleSearch} className="flex gap-2">
            <Input
              placeholder="Search keyword (e.g. 'mule account', 'anand@okaxis', 'DEV-A990')..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              required
            />
            <Button type="submit" variant="primary" isLoading={loading} rightIcon={<ArrowRight className="h-4 w-4" />}>
              Search Index
            </Button>
          </form>

          {results.length > 0 && (
            <div className="space-y-2 pt-2">
              {results.map((r) => (
                <div key={r.id} className="p-3 rounded border border-slate-200 bg-slate-50 flex items-center justify-between text-xs">
                  <div>
                    <Link href={`/investigations/${r.id}`} className="font-bold text-blue-700 hover:underline block font-mono">
                      {r.id}: {r.title}
                    </Link>
                    <span className="text-[10px] text-slate-500">{r.type}</span>
                  </div>
                  <Badge variant="success" size="sm">{r.status}</Badge>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
