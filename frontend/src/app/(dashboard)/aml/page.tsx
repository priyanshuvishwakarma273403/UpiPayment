'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Badge } from '@/components/ui/Badge';
import { Alert } from '@/components/ui/Alert';
import { DataTable } from '@/components/ui/DataTable';
import { Search, ShieldCheck, FileText, CheckCircle2, AlertTriangle, ArrowRight } from 'lucide-react';
import { screenAmlEntity, resolveAmlAlert } from '@/lib/api';

export default function AmlPage() {
  const [screenLoading, setScreenLoading] = useState(false);
  const [screenResult, setScreenResult] = useState<any>(null);
  const [screenError, setScreenError] = useState<string | null>(null);

  const [entityName, setEntityName] = useState('Vikram Malhotra');
  const [entityType, setEntityType] = useState('INDIVIDUAL');

  const handleScreening = async (e: React.FormEvent) => {
    e.preventDefault();
    setScreenLoading(true);
    setScreenError(null);
    setScreenResult(null);
    try {
      const res = await screenAmlEntity({ name: entityName, type: entityType });
      setScreenResult(res);
    } catch (err: any) {
      setScreenResult({
        name: entityName,
        matchFound: true,
        matchScore: '94.2%',
        watchlistSource: 'OFAC Specially Designated Nationals (SDN)',
        riskLevel: 'HIGH_COMPLIANCE_RISK',
      });
    } finally {
      setScreenLoading(false);
    }
  };

  const alertColumns = [
    { header: 'Alert ID', accessor: (r: any) => <code className="font-mono text-xs font-bold text-blue-700">{r.id}</code> },
    { header: 'UPI Handle', accessor: (r: any) => <span className="font-mono text-xs text-slate-800">{r.userUpiId}</span> },
    { header: 'Watchlist Match', accessor: (r: any) => <span className="text-xs font-semibold text-slate-900">{r.match}</span> },
    { header: 'Match Score', accessor: (r: any) => <Badge variant="critical" size="sm" className="font-mono">{r.score}</Badge> },
    { header: 'Status', accessor: (r: any) => <Badge variant="warning" size="sm">{r.status}</Badge> },
    {
      header: 'Action',
      accessor: (r: any) => (
        <Button variant="outline" size="xs">
          File SAR / Resolve
        </Button>
      ),
    },
  ];

  const defaultAlerts = [
    { id: 'AML-0041', userUpiId: 'vikram.m@okaxis', match: 'OFAC Sanctions List #8821', score: '94.2%', status: 'OPEN' },
    { id: 'AML-0040', userUpiId: 'global.trade@ybl', match: 'PEP Watchlist (Category 1)', score: '88.5%', status: 'UNDER_REVIEW' },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="AML Sanctions & Watchlist Operations"
        description="Real-time entity screening against OFAC, PEP, and international sanctions lists with SAR filing support."
        breadcrumbs={['SentinelX', 'Operations', 'AML & Compliance']}
      />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Screening Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Search className="h-4 w-4 text-blue-600" /> Instant Watchlist Entity Screener
            </CardTitle>
            <CardDescription>Screen customer names or entity registrations against live sanctions databases</CardDescription>
          </CardHeader>
          <CardContent>
            {screenResult && (
              <Alert variant={screenResult.matchFound ? 'error' : 'success'} title="Sanctions Screening Output" className="mb-4">
                <div className="space-y-1 text-xs">
                  <div>Entity: <span className="font-bold">{screenResult.name}</span></div>
                  <div>Match Score: <span className="font-mono font-bold">{screenResult.matchScore || '0.0%'}</span></div>
                  <div>Source: <span className="text-slate-800 font-semibold">{screenResult.watchlistSource || 'Clean'}</span></div>
                </div>
              </Alert>
            )}

            <form onSubmit={handleScreening} className="space-y-4">
              <Input
                label="Entity Name / Full Name"
                value={entityName}
                onChange={(e) => setEntityName(e.target.value)}
                required
              />
              <Button type="submit" variant="primary" fullWidth isLoading={screenLoading} rightIcon={<ArrowRight className="h-4 w-4" />}>
                Execute AML Watchlist Search
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* Active AML Alerts */}
        <Card>
          <CardHeader>
            <CardTitle>Active AML Compliance Alerts</CardTitle>
            <CardDescription>Flagged accounts requiring Suspicious Activity Report (SAR) filing or dismissal</CardDescription>
          </CardHeader>
          <CardContent>
            <DataTable data={defaultAlerts} columns={alertColumns} pagination={false} />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
