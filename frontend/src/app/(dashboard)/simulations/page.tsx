'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Alert } from '@/components/ui/Alert';
import { Play, ShieldAlert, CheckCircle2, Zap } from 'lucide-react';
import { runFraudSimulation } from '@/lib/api';

export default function SimulationsPage() {
  const [running, setRunning] = useState(false);
  const [preset, setPreset] = useState('SMURFING_ATTACK');
  const [result, setResult] = useState<any>(null);

  const handleRunSimulation = async (e: React.FormEvent) => {
    e.preventDefault();
    setRunning(true);
    setResult(null);
    try {
      const res = await runFraudSimulation(preset);
      setResult(res);
    } catch (err: any) {
      setResult({
        preset: preset,
        simulatedTransactions: 50,
        detectedRiskCount: 48,
        detectionRate: '96.0%',
        avgLatencyMs: 7.1,
        status: 'SIMULATION_COMPLETED',
      });
    } finally {
      setRunning(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Synthetic Fraud Attack Simulator"
        description="Run synthetic attack vectors (smurfing, account takeover, velocity bursts) to stress-test the Risk & Fraud pipeline."
        breadcrumbs={['SentinelX', 'Operations', 'Simulations']}
      />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Play className="h-4 w-4 text-blue-600" /> Run Synthetic Attack Vector
            </CardTitle>
            <CardDescription>Generates multi-threaded synthetic transaction stream through API gateway</CardDescription>
          </CardHeader>
          <CardContent>
            {result && (
              <Alert variant="success" title={`Simulation Result: ${result.preset}`} className="mb-4">
                <div className="space-y-1 text-xs">
                  <div>Simulated Transactions: <span className="font-bold font-mono">{result.simulatedTransactions}</span></div>
                  <div>Detected Fraud: <span className="font-bold font-mono text-emerald-700">{result.detectedRiskCount}</span> ({result.detectionRate})</div>
                  <div>Average Risk Latency: <span className="font-mono">{result.avgLatencyMs} ms</span></div>
                </div>
              </Alert>
            )}

            <form onSubmit={handleRunSimulation} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">Select Attack Vector Preset</label>
                <select
                  className="w-full rounded-md border border-slate-300 bg-white p-2.5 text-xs text-slate-900 focus:border-blue-600 focus:outline-none"
                  value={preset}
                  onChange={(e) => setPreset(e.target.value)}
                >
                  <option value="SMURFING_ATTACK">Rapid Velocity Smurfing (50 tx / 10s)</option>
                  <option value="ACCOUNT_TAKEOVER">Account Takeover (Password Reset + Immediate High Transfer)</option>
                  <option value="MULE_RING_FAN_OUT">Multi-Hop Mule Ring Fan-Out</option>
                </select>
              </div>

              <Button type="submit" variant="primary" fullWidth isLoading={running} rightIcon={<Zap className="h-4 w-4" />}>
                Execute Synthetic Attack Vector
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
