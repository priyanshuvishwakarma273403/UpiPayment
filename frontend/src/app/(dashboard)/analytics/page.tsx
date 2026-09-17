'use client';

import React from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { StatCard } from '@/components/ui/StatCard';
import { Badge } from '@/components/ui/Badge';
import { BarChart3, TrendingUp, Clock, Activity } from 'lucide-react';

export default function AnalyticsPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Platform Analytics & Executive Metrics"
        description="Comprehensive volume, risk distribution, precision rates, and P99 latency charts."
        breadcrumbs={['SentinelX', 'Operations', 'Analytics']}
      />

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Volume Processed"
          value="₹ 148.2 Cr"
          change="+18.4%"
          changeType="increase"
          comparisonText="Last 30 days window"
          icon={<TrendingUp className="h-4 w-4 text-blue-600" />}
          statusVariant="info"
        />
        <StatCard
          title="Fraud Prevention Rate"
          value="99.82%"
          change="High Precision"
          changeType="increase"
          comparisonText="Verified by investigator labels"
          icon={<Activity className="h-4 w-4 text-emerald-600" />}
          statusVariant="success"
        />
        <StatCard
          title="P99 Pipeline Latency"
          value="14.1 ms"
          change="Within SLA"
          changeType="increase"
          comparisonText="OpenTelemetry Tracing"
          icon={<Clock className="h-4 w-4 text-purple-600" />}
          statusVariant="success"
        />
        <StatCard
          title="Fraud Loss Avoided"
          value="₹ 2.4 Cr"
          change="340 Cases"
          changeType="increase"
          comparisonText="Saved by Risk Engine"
          icon={<BarChart3 className="h-4 w-4 text-indigo-600" />}
          statusVariant="neutral"
        />
      </div>
    </div>
  );
}
