'use client';

import React, { use } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { ArrowLeft, Search, ShieldCheck } from 'lucide-react';
import Link from 'next/link';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function InvestigationSearchResultPage({ params }: PageProps) {
  const { id } = use(params);

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Investigation Search Record: ${id}`}
        description="Indexed search result profile across payment ledgers, analyst notes, and compliance records."
        breadcrumbs={['SentinelX', 'Search', id]}
        action={
          <Link href="/cases">
            <Button variant="outline" size="sm" leftIcon={<ArrowLeft className="h-3.5 w-3.5" />}>
              Back to Cases
            </Button>
          </Link>
        }
      />

      <Card>
        <CardHeader>
          <CardTitle>Indexed Record Profile</CardTitle>
          <CardDescription>OpenSearch / Elasticsearch indexing status</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3 text-xs">
          <div className="flex justify-between p-3 rounded bg-slate-50 border border-slate-200">
            <span className="font-semibold text-slate-700">Record Key:</span>
            <code className="font-mono text-blue-700 font-bold">{id}</code>
          </div>
          <div className="flex justify-between p-3 rounded bg-slate-50 border border-slate-200">
            <span className="font-semibold text-slate-700">Index Status:</span>
            <Badge variant="success" size="sm">INDEXED_OK</Badge>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
