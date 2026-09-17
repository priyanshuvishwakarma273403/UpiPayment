'use client';

import React, { useEffect, useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Input } from '@/components/ui/Input';
import { Network, Search, RefreshCw, AlertTriangle, ShieldCheck, UserCheck, Smartphone, Building2, User, Share2, ArrowRight } from 'lucide-react';
import { fetchFraudNetworkClusters } from '@/lib/api';
import Link from 'next/link';

interface NodeDetail {
  id: string;
  name: string;
  type: 'CUSTOMER' | 'DEVICE' | 'BANK_ACCOUNT' | 'MERCHANT';
  riskScore: number;
  connectionsCount: number;
  flaggedReason: string;
}

import { GsapNetworkCanvas } from '@/components/network/GsapNetworkCanvas';

export default function NetworkPage() {
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState('');
  const [clusters, setClusters] = useState<any[]>([]);
  const [selectedNode, setSelectedNode] = useState<NodeDetail>({
    id: 'CUST-4091',
    name: 'Anand Kumar Sharma',
    type: 'CUSTOMER',
    riskScore: 88,
    connectionsCount: 7,
    flaggedReason: 'Shared device fingerprint dev_99a8b1 with 2 reported mule accounts.',
  });

  const loadNetwork = async () => {
    setLoading(true);
    try {
      const res = await fetchFraudNetworkClusters();
      setClusters(Array.isArray(res) ? res : []);
    } catch (err) {
      setClusters([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNetwork();
  }, []);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Multi-Hop Network Entity Graph"
        description="Sardine-style fraud ring detection, entity traversal, and shared device & account cluster graph analysis."
        breadcrumbs={['SentinelX', 'Operations', 'Network Graph']}
        action={
          <Button variant="outline" size="sm" onClick={loadNetwork} isLoading={loading} leftIcon={<RefreshCw className="h-3.5 w-3.5" />}>
            Refresh Graph Nodes
          </Button>
        }
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Interactive Graph View (2 cols) */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <CardTitle className="flex items-center gap-2">
                  <Network className="h-4 w-4 text-indigo-600 dark:text-indigo-400" /> Multi-Hop Entity Relationship Canvas
                </CardTitle>
                <CardDescription>Sub-10ms graph traversal engine across customer and device nodes</CardDescription>
              </div>
              <div className="w-64">
                <Input
                  placeholder="Search Node ID or UPI Handle..."
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  leftIcon={<Search className="h-3.5 w-3.5 text-slate-400" />}
                />
              </div>
            </CardHeader>

            <CardContent className="space-y-4">
              {/* GSAP Animated Data Flow Pipeline */}
              <GsapNetworkCanvas />

              {/* High Resolution Network Visualization Graphic */}
              <div className="relative rounded-xl overflow-hidden border border-slate-200 dark:border-slate-800 shadow-md group bg-slate-900">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src="/investigation-network.jpg"
                  alt="SentinelX Multi-Hop Investigation Network Relationship Graph"
                  className="w-full h-auto object-cover rounded-xl transition-transform duration-500 group-hover:scale-102 opacity-90"
                />
                <div className="absolute top-3 right-3 bg-slate-900/90 backdrop-blur-sm text-white px-3 py-1.5 rounded-md text-[11px] font-mono flex items-center gap-2 border border-slate-700">
                  <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
                  <span>Graph Traversal SLA: 4.1ms</span>
                </div>
              </div>

              {/* Interactive Node Selector Canvas */}
              <div className="rounded-xl bg-slate-950 p-4 relative overflow-hidden flex flex-col justify-between text-white border border-slate-800 shadow-inner space-y-4">
                <div className="relative z-10 flex items-center justify-between text-xs font-mono">
                  <span className="text-emerald-400 flex items-center gap-1.5">
                    <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" /> Selected Graph Entity Cluster #MULE-8832
                  </span>
                  <span className="text-slate-400">14 Linked Nodes</span>
                </div>

                {/* Conceptual Interactive Node Chips */}
                <div className="relative z-10 grid grid-cols-1 sm:grid-cols-3 gap-3">
                  <div
                    onClick={() =>
                      setSelectedNode({
                        id: 'CUST-4091',
                        name: 'Anand Kumar Sharma',
                        type: 'CUSTOMER',
                        riskScore: 88,
                        connectionsCount: 7,
                        flaggedReason: 'Rapid 5m velocity spike from VPN exit node.',
                      })
                    }
                    className={`cursor-pointer p-3 rounded-xl border text-center font-mono text-xs transition-all ${
                      selectedNode.id === 'CUST-4091'
                        ? 'bg-rose-950/80 border-rose-500 text-rose-200 ring-2 ring-rose-500/50 scale-105'
                        : 'bg-rose-950/30 border-rose-900/50 text-rose-300 hover:border-rose-500'
                    }`}
                  >
                    <User className="h-4 w-4 mx-auto mb-1 text-rose-400" />
                    <div className="font-bold">anand@okaxis</div>
                    <div className="text-[10px] text-rose-300 mt-0.5">Customer (88/100)</div>
                  </div>

                  <div
                    onClick={() =>
                      setSelectedNode({
                        id: 'DEV-A990-21X',
                        name: 'Samsung S23 Android 14',
                        type: 'DEVICE',
                        riskScore: 92,
                        connectionsCount: 4,
                        flaggedReason: 'Device fingerprint shared across 4 distinct bank accounts.',
                      })
                    }
                    className={`cursor-pointer p-3 rounded-xl border text-center font-mono text-xs transition-all ${
                      selectedNode.id === 'DEV-A990-21X'
                        ? 'bg-amber-950/80 border-amber-500 text-amber-200 ring-2 ring-amber-500/50 scale-105'
                        : 'bg-amber-950/30 border-amber-900/50 text-amber-300 hover:border-amber-500'
                    }`}
                  >
                    <Smartphone className="h-4 w-4 mx-auto mb-1 text-amber-400" />
                    <div className="font-bold">DEV-A990-21X</div>
                    <div className="text-[10px] text-amber-300 mt-0.5">Shared Device (92/100)</div>
                  </div>

                  <div
                    onClick={() =>
                      setSelectedNode({
                        id: 'MERCH-STORE-YBL',
                        name: 'Global Store Merchant',
                        type: 'MERCHANT',
                        riskScore: 78,
                        connectionsCount: 12,
                        flaggedReason: 'Recipient of 14 rapid high-value transactions in 1 hour.',
                      })
                    }
                    className={`cursor-pointer p-3 rounded-xl border text-center font-mono text-xs transition-all ${
                      selectedNode.id === 'MERCH-STORE-YBL'
                        ? 'bg-indigo-950/80 border-indigo-500 text-indigo-200 ring-2 ring-indigo-500/50 scale-105'
                        : 'bg-indigo-950/30 border-indigo-900/50 text-indigo-300 hover:border-indigo-500'
                    }`}
                  >
                    <Building2 className="h-4 w-4 mx-auto mb-1 text-indigo-400" />
                    <div className="font-bold">store@ybl</div>
                    <div className="text-[10px] text-indigo-300 mt-0.5">Beneficiary (78/100)</div>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Column: Node Details & High Risk Clusters */}
        <div className="space-y-6">
          {/* Selected Node Details Card */}
          <Card className="border-indigo-200 dark:border-indigo-900/50">
            <CardHeader className="pb-3">
              <CardTitle className="text-sm font-bold flex items-center justify-between">
                <span>Selected Entity Inspection</span>
                <Badge variant={selectedNode.riskScore >= 75 ? 'danger' : 'warning'}>
                  {selectedNode.riskScore}/100 RISK
                </Badge>
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-xs">
              <div className="space-y-1">
                <span className="text-slate-400 font-mono text-[10px] uppercase block">Entity Name</span>
                <span className="font-bold text-slate-900 dark:text-slate-100 text-sm">{selectedNode.name}</span>
                <span className="font-mono text-indigo-600 dark:text-indigo-400 block">{selectedNode.id}</span>
              </div>

              <div className="rounded-lg bg-slate-50 dark:bg-slate-800/60 p-3 space-y-1.5 border border-slate-200 dark:border-slate-700">
                <div className="flex justify-between text-[11px]">
                  <span className="text-slate-500">Connections:</span>
                  <span className="font-bold font-mono">{selectedNode.connectionsCount} Linked Nodes</span>
                </div>
                <div className="flex justify-between text-[11px]">
                  <span className="text-slate-500">Node Type:</span>
                  <span className="font-semibold">{selectedNode.type}</span>
                </div>
              </div>

              <div className="rounded-lg bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900/60 p-3 text-rose-900 dark:text-rose-300 text-[11px] leading-relaxed">
                <strong className="block font-semibold mb-0.5">Flagged Reason:</strong>
                {selectedNode.flaggedReason}
              </div>

              {selectedNode.type === 'CUSTOMER' && (
                <Link href={`/customers/${selectedNode.id}`}>
                  <Button variant="primary" size="sm" className="w-full text-xs" rightIcon={<ArrowRight className="h-3.5 w-3.5" />}>
                    Open Customer 360 Profile
                  </Button>
                </Link>
              )}
            </CardContent>
          </Card>

          {/* High-Risk Clusters */}
          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-bold flex items-center gap-2">
                <AlertTriangle className="h-4 w-4 text-rose-600" /> High-Risk Mule Clusters
              </CardTitle>
              <CardDescription>Detected financial crime network rings</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3 text-xs">
              {[
                { id: 'CLUSTER-8832', nodes: 14, risk: 'CRITICAL', exposure: '₹ 14,80,000' },
                { id: 'CLUSTER-8831', nodes: 8, risk: 'HIGH', exposure: '₹ 6,20,000' },
                { id: 'CLUSTER-8829', nodes: 5, risk: 'MEDIUM', exposure: '₹ 1,50,000' },
              ].map((c) => (
                <div key={c.id} className="p-3 rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 space-y-1">
                  <div className="flex items-center justify-between font-mono">
                    <span className="font-bold text-indigo-600 dark:text-indigo-400">{c.id}</span>
                    <Badge variant={c.risk === 'CRITICAL' ? 'danger' : 'warning'} size="sm">
                      {c.risk}
                    </Badge>
                  </div>
                  <div className="flex justify-between text-[11px] text-slate-600 dark:text-slate-400">
                    <span>Connected Nodes: <strong>{c.nodes}</strong></span>
                    <span>Exposure: <strong className="text-slate-900 dark:text-slate-100">{c.exposure}</strong></span>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
