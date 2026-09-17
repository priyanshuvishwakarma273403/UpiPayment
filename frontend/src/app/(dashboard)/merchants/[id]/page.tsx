'use client';

import React, { use } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { ArrowLeft, Building, QrCode } from 'lucide-react';
import Link from 'next/link';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function MerchantDetailPage({ params }: PageProps) {
  const { id } = use(params);

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Merchant Profile: ${id}`}
        description="Dynamic QR code payload configuration, risk metadata, and settlement history."
        breadcrumbs={['SentinelX', 'Merchants', id]}
        action={
          <Link href="/merchants">
            <Button variant="outline" size="sm" leftIcon={<ArrowLeft className="h-3.5 w-3.5" />}>
              Back to Merchants
            </Button>
          </Link>
        }
      />

      <Card>
        <CardHeader>
          <CardTitle>Merchant QR Code Generation</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4 text-xs">
          <div className="flex items-center gap-4">
            <div className="p-4 rounded-lg bg-slate-900 text-white flex flex-col items-center justify-center">
              <QrCode className="h-16 w-16 text-blue-400" />
              <span className="text-[10px] font-mono mt-2 text-slate-400">Dynamic Payload QR</span>
            </div>
            <div className="space-y-2">
              <div><span className="text-slate-500">Merchant Name:</span> <span className="font-bold text-slate-900">Apex Electronics</span></div>
              <div><span className="text-slate-500">UPI Handle:</span> <code className="font-mono text-blue-700 font-semibold">apex@ybl</code></div>
              <div><span className="text-slate-500">Settlement Tier:</span> <Badge variant="success" size="sm">T+0 SAME DAY</Badge></div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
