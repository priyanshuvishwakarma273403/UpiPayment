'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import { BrainCircuit, RefreshCw, Activity, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { useToast } from '@/lib/useToast';
import confetti from 'canvas-confetti';

const driftData = [
  { day: 'Day 1', accuracy: 98.4, psi: 0.02 },
  { day: 'Day 5', accuracy: 98.1, psi: 0.04 },
  { day: 'Day 10', accuracy: 97.5, psi: 0.08 },
  { day: 'Day 15', accuracy: 96.8, psi: 0.14 },
  { day: 'Day 20', accuracy: 95.2, psi: 0.22 }, // Drift warning threshold > 0.20
];

export default function ModelDriftPage() {
  const { toast } = useToast();
  const [retraining, setRetraining] = useState(false);

  const triggerAutoRetrain = () => {
    setRetraining(true);
    toast('Auto-Retrain Job Submitted', 'FastAPI ML Engine retraining model on past 30 days verified fraud labels.', 'info');

    setTimeout(() => {
      setRetraining(false);
      toast('ML Model Retrained Successfully', 'Model XGBoost v4.3 deployed. Accuracy restored to 98.6%.', 'success');
      confetti({ particleCount: 50, spread: 60, origin: { y: 0.7 } });
    }, 1200);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="ML Model Concept Drift & PSI Inspector"
        description="Monitor machine learning performance decay, Population Stability Index (PSI), and automated model retraining triggers."
        breadcrumbs={['SentinelX', 'Model Ops', 'Concept Drift']}
        action={
          <Button variant="primary" size="sm" onClick={triggerAutoRetrain} isLoading={retraining} leftIcon={<RefreshCw className="h-3.5 w-3.5" />}>
            Auto-Retrain Model
          </Button>
        }
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Accuracy Decay Chart */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-bold flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <BrainCircuit className="h-4 w-4 text-indigo-600 dark:text-indigo-400" /> Model Accuracy vs Population Stability Index (PSI)
                </span>
                <Badge variant="warning" className="font-mono text-[10px]">
                  PSI THRESHOLD EXCEEDED (0.22)
                </Badge>
              </CardTitle>
              <CardDescription>Live telemetry from FastAPI ML Engine v4.2</CardDescription>
            </CardHeader>

            <CardContent>
              <div className="h-64 w-full pt-2">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={driftData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                    <XAxis dataKey="day" tick={{ fontSize: 11 }} />
                    <YAxis domain={[90, 100]} tick={{ fontSize: 11 }} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: '#0f172a',
                        borderColor: '#334155',
                        borderRadius: '8px',
                        color: '#fff',
                        fontSize: '12px',
                      }}
                    />
                    <Line type="monotone" dataKey="accuracy" name="Accuracy %" stroke="#6366f1" strokeWidth={2.5} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right 1 Col: Drift Status Metrics */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-bold">Concept Drift Diagnostic</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-xs font-mono">
              <div className="p-3 rounded-lg bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-900/60 text-amber-900 dark:text-amber-300 space-y-1">
                <span className="font-bold block">Drift Warning:</span>
                New UPI velocity patterns detected in Mumbai subnets. Model retrain recommended.
              </div>

              <div className="space-y-1 text-[11px]">
                <div className="flex justify-between py-1 border-b border-slate-200 dark:border-slate-800">
                  <span className="text-slate-500">Current Model Version:</span>
                  <span className="font-bold">v4.2 (XGBoost)</span>
                </div>
                <div className="flex justify-between py-1 border-b border-slate-200 dark:border-slate-800">
                  <span className="text-slate-500">PSI Value:</span>
                  <span className="font-bold text-amber-600">0.22 (Moderate Drift)</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
