'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Input } from '@/components/ui/Input';
import { Network, Search, RefreshCw, Sparkles, AlertTriangle, ShieldCheck, Zap, Share2 } from 'lucide-react';
import { useToast } from '@/lib/useToast';

interface GnnNode {
  id: string;
  label: string;
  tsneX: number; // 0 to 100
  tsneY: number; // 0 to 100
  clusterId: string;
  riskScore: number;
  isMuleRing: boolean;
}

export default function GnnPage() {
  const { toast } = useToast();
  const [selectedCluster, setSelectedCluster] = useState<string>('RING-8902');

  const gnnNodes: GnnNode[] = [
    { id: 'N-1', label: 'anand@okaxis', tsneX: 25, tsneY: 30, clusterId: 'RING-8902', riskScore: 92, isMuleRing: true },
    { id: 'N-2', label: 'DEV-A990', tsneX: 30, tsneY: 35, clusterId: 'RING-8902', riskScore: 95, isMuleRing: true },
    { id: 'N-3', label: 'store@ybl', tsneX: 28, tsneY: 22, clusterId: 'RING-8902', riskScore: 88, isMuleRing: true },
    { id: 'N-4', label: 'priya@paytm', tsneX: 75, tsneY: 80, clusterId: 'SAFE-01', riskScore: 12, isMuleRing: false },
    { id: 'N-5', label: 'rahul@ybl', tsneX: 82, tsneY: 72, clusterId: 'SAFE-01', riskScore: 15, isMuleRing: false },
    { id: 'N-6', label: 'mule_hub@sbi', tsneX: 22, tsneY: 38, clusterId: 'RING-8902', riskScore: 98, isMuleRing: true },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Graph Neural Network (GNN) Ring Classifier & t-SNE Embeddings"
        description="GraphSAGE & Heterogeneous Graph Transformer 128-D vector embeddings projected on t-SNE space to detect hidden mule clusters."
        breadcrumbs={['SentinelX', 'Network Operations', 'GNN Classifier']}
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Interactive t-SNE Embeddings Canvas */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <div>
                <CardTitle className="flex items-center gap-2 text-sm font-bold">
                  <Sparkles className="h-4 w-4 text-indigo-600 dark:text-indigo-400" /> 128-D Node Vector Embeddings (t-SNE Projection)
                </CardTitle>
                <CardDescription>Automated shadow circles highlight hidden fraud ring boundaries</CardDescription>
              </div>
              <Badge variant="danger" className="font-mono text-[10px]">
                GNN MODEL v3.8 ACTIVE
              </Badge>
            </CardHeader>

            <CardContent>
              {/* t-SNE Scatter Plot Canvas Container */}
              <div className="relative h-96 w-full rounded-xl bg-slate-950 border border-slate-800 p-4 overflow-hidden text-white shadow-2xl">
                <div className="absolute inset-0 bg-[radial-gradient(#334155_1px,transparent_1px)] [background-size:20px_20px] opacity-25" />

                {/* Mule Ring Shadow Circle Highlight */}
                <div
                  className="absolute rounded-full border-2 border-dashed border-rose-500 bg-rose-500/10 animate-pulse pointer-events-none"
                  style={{ left: '15%', top: '15%', width: '35%', height: '45%' }}
                >
                  <span className="absolute top-2 left-2 font-mono text-[9px] text-rose-400 font-bold bg-rose-950/80 px-2 py-0.5 rounded border border-rose-800">
                    DETECTED MULE CLUSTER #RING-8902
                  </span>
                </div>

                {/* Render t-SNE Scatter Nodes */}
                {gnnNodes.map((n) => (
                  <div
                    key={n.id}
                    onClick={() => {
                      setSelectedCluster(n.clusterId);
                      toast(`Node Inspected: ${n.label}`, `GNN Embedding Score: ${n.riskScore}/100 in Cluster ${n.clusterId}`, n.isMuleRing ? 'danger' : 'info');
                    }}
                    className="absolute cursor-pointer transform -translate-x-1/2 -translate-y-1/2 group"
                    style={{ left: `${n.tsneX}%`, top: `${n.tsneY}%` }}
                  >
                    <div
                      className={`h-4 w-4 rounded-full border-2 transition-transform group-hover:scale-150 ${
                        n.isMuleRing
                          ? 'bg-rose-500 border-white shadow-[0_0_12px_#ef4444]'
                          : 'bg-emerald-400 border-white shadow-[0_0_10px_#10b981]'
                      }`}
                    />
                    <span className="absolute top-5 left-1/2 -translate-x-1/2 font-mono text-[9px] font-bold text-slate-200 whitespace-nowrap bg-slate-900/90 px-1.5 py-0.5 rounded border border-slate-800">
                      {n.label}
                    </span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right 1 Col: Cluster Inspection Panel */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-bold flex items-center justify-between">
                <span>Cluster #RING-8902 Metrics</span>
                <Badge variant="danger">95% FRAUD RING PROBABILITY</Badge>
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-xs">
              <div className="p-3 rounded-lg bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/60 text-rose-900 dark:text-rose-300 space-y-1 leading-relaxed">
                <strong className="block font-semibold">GNN Structural Embedding Anomaly:</strong>
                High cosine similarity (0.94) across 4 node vectors with shared device hash and 1-minute velocity spikes.
              </div>

              <div className="space-y-1 font-mono text-[11px]">
                <div className="flex justify-between py-1 border-b border-slate-200 dark:border-slate-800">
                  <span className="text-slate-500">Nodes Count:</span>
                  <span className="font-bold">4 Linked Entities</span>
                </div>
                <div className="flex justify-between py-1 border-b border-slate-200 dark:border-slate-800">
                  <span className="text-slate-500">GNN Model Architecture:</span>
                  <span className="font-bold">Heterogeneous Graph Transformer</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
