'use client';

import React from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Cpu, CheckCircle2, Sliders } from 'lucide-react';

export default function ModelsPage() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Machine Learning Model Registry & Artifacts"
        description="Scikit-learn and XGBoost model artifacts, MLflow run tracking, and production approvals."
        breadcrumbs={['SentinelX', 'Operations', 'Model Registry']}
      />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Cpu className="h-4 w-4 text-blue-600" /> Active Machine Learning Models
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3 text-xs">
          {[
            { version: 'v2.3.8-xgb', type: 'XGBoost Classifier', precision: '99.82%', status: 'PRODUCTION' },
            { version: 'v2.4.1-xgb', type: 'XGBoost Classifier', precision: '99.84%', status: 'CANDIDATE' },
          ].map((m) => (
            <div key={m.version} className="p-3 rounded border border-slate-200 bg-slate-50 flex items-center justify-between font-mono">
              <div>
                <span className="font-bold text-blue-700 block">{m.version}</span>
                <span className="text-[10px] text-slate-500">{m.type} • Precision: {m.precision}</span>
              </div>
              <Badge variant={m.status === 'PRODUCTION' ? 'success' : 'info'} size="sm">
                {m.status}
              </Badge>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
