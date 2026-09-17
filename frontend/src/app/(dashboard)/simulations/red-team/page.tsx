'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Flame, ShieldAlert, Zap, Play, CheckCircle2, RefreshCw, Activity } from 'lucide-react';
import { useToast } from '@/lib/useToast';
import confetti from 'canvas-confetti';

export default function RedTeamPage() {
  const { toast } = useToast();
  const [isRunning, setIsRunning] = useState(false);
  const [score, setScore] = useState({ redTeam: 14, blueTeam: 98 });

  const runRedTeamAttack = (attackType: string) => {
    setIsRunning(true);
    toast(`Red Team Attack Launched: ${attackType}`, 'Injecting 5,000 synthetic malicious payloads...', 'warning');

    setTimeout(() => {
      setIsRunning(false);
      setScore((prev) => ({ redTeam: prev.redTeam + 2, blueTeam: prev.blueTeam + 5 }));
      toast('SentinelX Defense Responded', 'Blocked 99.4% of adversary payloads. Engine SLA maintained < 8ms.', 'success');
      confetti({ particleCount: 60, spread: 70, origin: { y: 0.5 } });
    }, 1500);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Automated Fraud Red-Team Simulator & Adversary Testing"
        description="Simulate real-time credential stuffing, distributed bot bursts, and ATO attacks to test SentinelX defense resilience."
        breadcrumbs={['SentinelX', 'Simulations', 'Red-Team Sandbox']}
      />

      {/* Live Scoreboard Header */}
      <Card className="bg-slate-950 text-white border-slate-800">
        <CardContent className="p-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 border-b border-slate-800 pb-6">
            <div className="flex items-center gap-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-rose-600 text-white font-bold shadow-lg">
                <Flame className="h-6 w-6" />
              </div>
              <div>
                <h3 className="text-base font-bold font-mono">RED-TEAM vs SENTINELX BLUE-TEAM LIVE SCOREBOARD</h3>
                <p className="text-xs text-slate-400">Continuous Automated Adversary Simulation Engine</p>
              </div>
            </div>

            <div className="flex items-center gap-6 font-mono">
              <div className="text-center">
                <span className="text-[10px] text-rose-400 uppercase block">Red Team (Attacker)</span>
                <span className="text-xl font-bold text-rose-500">{score.redTeam} Breaches</span>
              </div>
              <span className="text-slate-700 text-xl font-bold">:</span>
              <div className="text-center">
                <span className="text-[10px] text-emerald-400 uppercase block">SentinelX Defense</span>
                <span className="text-xl font-bold text-emerald-400">{score.blueTeam} Defenses</span>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-4">
            <Button
              variant="outline"
              size="sm"
              className="border-rose-500/50 text-rose-300 hover:bg-rose-950 text-xs"
              onClick={() => runRedTeamAttack('Credential Stuffing Burst (10k req/s)')}
              isLoading={isRunning}
              leftIcon={<Play className="h-3.5 w-3.5" />}
            >
              Simulate Credential Stuffing
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="border-amber-500/50 text-amber-300 hover:bg-amber-950 text-xs"
              onClick={() => runRedTeamAttack('Distributed Mule Liquidation Campaign')}
              isLoading={isRunning}
              leftIcon={<Play className="h-3.5 w-3.5" />}
            >
              Simulate Mule Liquidation
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="border-indigo-500/50 text-indigo-300 hover:bg-indigo-950 text-xs"
              onClick={() => runRedTeamAttack('Account Takeover (ATO) GPS Jump')}
              isLoading={isRunning}
              leftIcon={<Play className="h-3.5 w-3.5" />}
            >
              Simulate ATO Geofence Jump
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
