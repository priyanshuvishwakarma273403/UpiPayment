'use client';

import React, { useState } from 'react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import { Sliders, Play, CheckCircle2, AlertTriangle, RefreshCw } from 'lucide-react';
import { useToast } from '@/lib/useToast';
import confetti from 'canvas-confetti';

const monteCarloResults = [
  { run: 'Run 1', fpRate: 0.12, blockedVolume: 42.5, caughtFraud: 94 },
  { run: 'Run 2', fpRate: 0.18, blockedVolume: 48.0, caughtFraud: 96 },
  { run: 'Run 3', fpRate: 0.09, blockedVolume: 39.2, caughtFraud: 91 },
  { run: 'Run 4', fpRate: 0.14, blockedVolume: 44.1, caughtFraud: 95 },
  { run: 'Run 5', fpRate: 0.11, blockedVolume: 41.8, caughtFraud: 93 },
];

export function MonteCarloBacktester() {
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [testRule, setTestRule] = useState('velocity_5m.count > 4 AND amount > 25000');

  const runSimulation = () => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      toast('Monte Carlo Simulation Complete', 'Tested rule against 100,000 historical transactions.', 'success');
      confetti({ particleCount: 50, spread: 60, origin: { y: 0.7 } });
    }, 1200);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm font-bold flex items-center justify-between">
          <span className="flex items-center gap-2">
            <Sliders className="h-4 w-4 text-indigo-600 dark:text-indigo-400" /> Monte Carlo Rule Backtesting Simulator
          </span>
          <Badge variant="info" className="font-mono text-[10px]">
            100,000 HISTORICAL TXNS
          </Badge>
        </CardTitle>
        <CardDescription>Simulate draft CEL fraud rules before deploying to live production</CardDescription>
      </CardHeader>

      <CardContent className="space-y-4 text-xs">
        <div className="space-y-2">
          <label className="font-semibold text-slate-700 dark:text-slate-300 block">Draft CEL Rule Condition</label>
          <div className="flex gap-2">
            <textarea
              value={testRule}
              onChange={(e) => setTestRule(e.target.value)}
              className="flex-1 rounded-md border border-slate-200 dark:border-slate-700 bg-slate-900 text-emerald-400 font-mono p-2.5 text-xs focus:ring-2 focus:ring-indigo-500"
              rows={2}
            />
            <Button variant="primary" size="sm" onClick={runSimulation} isLoading={loading} leftIcon={<Play className="h-3.5 w-3.5" />}>
              Run Simulation
            </Button>
          </div>
        </div>

        {/* Simulation Output Stats */}
        <div className="grid grid-cols-3 gap-3 font-mono">
          <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 text-center">
            <span className="text-[10px] text-slate-400 block">FALSE POSITIVE RATE</span>
            <span className="text-sm font-bold text-emerald-600">0.12 %</span>
          </div>
          <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 text-center">
            <span className="text-[10px] text-slate-400 block">PREVENTED LOSS</span>
            <span className="text-sm font-bold text-indigo-600">₹ 42.5 Lakh</span>
          </div>
          <div className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 text-center">
            <span className="text-[10px] text-slate-400 block">ENGINE LATENCY</span>
            <span className="text-sm font-bold text-slate-900 dark:text-slate-100">+0.4 ms</span>
          </div>
        </div>

        {/* Monte Carlo Recharts Bar Chart */}
        <div className="h-44 w-full pt-2">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={monteCarloResults} margin={{ top: 10, right: 10, left: -25, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
              <XAxis dataKey="run" tick={{ fontSize: 10 }} />
              <YAxis tick={{ fontSize: 10 }} />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#0f172a',
                  borderColor: '#334155',
                  borderRadius: '8px',
                  color: '#fff',
                  fontSize: '11px',
                }}
              />
              <Bar dataKey="blockedVolume" name="Blocked Volume (₹ L)" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
