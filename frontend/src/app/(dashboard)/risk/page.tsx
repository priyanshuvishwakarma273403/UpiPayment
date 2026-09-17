'use client';

import React, { useEffect, useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Select } from '@/components/ui/Select';
import { Badge } from '@/components/ui/Badge';
import { Alert } from '@/components/ui/Alert';
import { StatCard } from '@/components/ui/StatCard';
import { DataTable } from '@/components/ui/DataTable';
import { Drawer } from '@/components/ui/Drawer';
import { MonteCarloBacktester } from '@/components/risk/MonteCarloBacktester';
import {
  Zap,
  Sliders,
  ShieldCheck,
  RefreshCw,
  Plus,
  Play,
  Search,
  Filter,
  CheckCircle,
  AlertTriangle,
  Flame,
  Settings,
  Code,
  Eye,
  Layers,
} from 'lucide-react';
import { calculateRiskScore, fetchRiskRules } from '@/lib/api';
import { useToast } from '@/lib/useToast';

interface RuleItem {
  id: string;
  code: string;
  name: string;
  category: 'VELOCITY' | 'DEVICE' | 'GEOFENCE' | 'BEHAVIORAL' | 'IDENTITY';
  condition: string;
  action: 'BLOCK' | 'REVIEW' | 'SCORE_ADD';
  scoreImpact: number;
  status: 'ACTIVE' | 'TESTING' | 'SHADOW_MODE';
  triggers24h: number;
  fpRate: number;
}

export default function RiskPage() {
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');

  // Rule Editor Drawer state
  const [isEditorOpen, setIsEditorOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<Partial<RuleItem>>({
    code: 'R-NEW',
    name: 'New Custom Fraud Rule',
    category: 'VELOCITY',
    condition: 'velocity_1h.count > 5 AND amount > 25000',
    action: 'SCORE_ADD',
    scoreImpact: 35,
    status: 'SHADOW_MODE',
  });

  const defaultRules: RuleItem[] = [
    { id: '1', code: 'R-109', name: 'High Rolling Velocity Burst (5m)', category: 'VELOCITY', condition: 'txn_count_5m > 4 AND total_amount_5m > 50000', action: 'BLOCK', scoreImpact: 90, status: 'ACTIVE', triggers24h: 142, fpRate: 0.12 },
    { id: '2', code: 'R-204', name: 'New Device + Proxy IP Combination', category: 'DEVICE', condition: 'device.is_new == true AND ip.is_vpn_proxy == true', action: 'BLOCK', scoreImpact: 85, status: 'ACTIVE', triggers24h: 89, fpRate: 0.28 },
    { id: '3', code: 'R-301', name: 'Impossible Speed Travel / Geofence Jump', category: 'GEOFENCE', condition: 'geo_distance_km > 500 AND elapsed_minutes < 15', action: 'REVIEW', scoreImpact: 65, status: 'SHADOW_MODE', triggers24h: 34, fpRate: 0.45 },
    { id: '4', code: 'R-401', name: 'Beneficiary Linkage to Reported Mule Cluster', category: 'IDENTITY', condition: 'beneficiary.mule_cluster_distance <= 1', action: 'BLOCK', scoreImpact: 95, status: 'ACTIVE', triggers24h: 17, fpRate: 0.05 },
  ];

  const [ruleList, setRuleList] = useState<RuleItem[]>(defaultRules);

  const filteredRules = ruleList.filter((r) => {
    const matchesSearch =
      !searchQuery ||
      r.code.toLowerCase().includes(searchQuery.toLowerCase()) ||
      r.name.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCat = categoryFilter === 'ALL' || r.category === categoryFilter;
    return matchesSearch && matchesCat;
  });

  const handleSaveRule = (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingRule.name || !editingRule.code) return;

    const newObj: RuleItem = {
      id: String(Date.now()),
      code: editingRule.code || 'R-100',
      name: editingRule.name || 'Custom Rule',
      category: editingRule.category || 'VELOCITY',
      condition: editingRule.condition || 'true',
      action: editingRule.action || 'SCORE_ADD',
      scoreImpact: editingRule.scoreImpact || 25,
      status: editingRule.status || 'SHADOW_MODE',
      triggers24h: 0,
      fpRate: 0.0,
    };

    setRuleList([newObj, ...ruleList]);
    setIsEditorOpen(false);
    toast('Rule Published', `Rule ${newObj.code} active in ${newObj.status}.`, 'success');
  };

  const columns = [
    {
      header: 'Rule Code',
      accessor: (r: RuleItem) => (
        <span className="font-mono text-xs font-bold text-indigo-600 dark:text-indigo-400">
          {r.code}
        </span>
      ),
    },
    {
      header: 'Rule Description',
      accessor: (r: RuleItem) => (
        <div>
          <span className="text-xs font-semibold text-slate-900 dark:text-slate-100 block">
            {r.name}
          </span>
          <span className="font-mono text-[11px] text-slate-500 dark:text-slate-400 truncate max-w-xs block">
            {r.condition}
          </span>
        </div>
      ),
    },
    {
      header: 'Category',
      accessor: (r: RuleItem) => (
        <Badge variant="info" size="sm">
          {r.category}
        </Badge>
      ),
    },
    {
      header: 'Deployment Status',
      accessor: (r: RuleItem) => (
        <Badge variant={r.status === 'ACTIVE' ? 'success' : r.status === 'SHADOW_MODE' ? 'warning' : 'neutral'} size="sm">
          {r.status === 'SHADOW_MODE' ? 'SHADOW (SILENT)' : r.status}
        </Badge>
      ),
    },
    {
      header: 'Triggers (24h)',
      accessor: (r: RuleItem) => (
        <span className="font-mono text-xs font-semibold text-slate-800 dark:text-slate-200">
          {r.triggers24h.toLocaleString()}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Enterprise Fraud Rule Engine & Policy Manager"
        description="Hot-reloaded CEL detection logic, rule shadow mode dark deployment, and Monte Carlo backtesting simulator."
        breadcrumbs={['SentinelX', 'Operations', 'Risk Rules']}
        action={
          <Button
            variant="primary"
            size="sm"
            onClick={() => {
              setEditingRule({
                code: `R-${Math.floor(100 + Math.random() * 900)}`,
                name: 'New Custom Velocity Rule',
                category: 'VELOCITY',
                condition: 'velocity_1h.count > 5 AND amount > 25000',
                action: 'SCORE_ADD',
                scoreImpact: 35,
                status: 'SHADOW_MODE',
              });
              setIsEditorOpen(true);
            }}
            leftIcon={<Plus className="h-3.5 w-3.5" />}
          >
            Create New Rule
          </Button>
        }
      />

      {/* Top Stat Banner */}
      <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <StatCard
          title="Active Scoring Rules"
          value={`${ruleList.filter((r) => r.status === 'ACTIVE').length} Active`}
          change="Hot-Reloaded"
          changeType="increase"
          comparisonText="Spring Boot Engine SLA < 10ms"
          icon={<Zap className="h-4 w-4 text-emerald-600" />}
          statusVariant="success"
        />
        <StatCard
          title="Shadow Mode Rules"
          value={`${ruleList.filter((r) => r.status === 'SHADOW_MODE').length} Shadow`}
          change="Silent Testing"
          changeType="neutral"
          comparisonText="Dark deployed for validation"
          icon={<Layers className="h-4 w-4 text-amber-600" />}
          statusVariant="warning"
        />
        <StatCard
          title="Avg False Positive Rate"
          value="0.12 %"
          change="-0.08% vs last week"
          changeType="increase"
          comparisonText="Machine Learning Verified"
          icon={<CheckCircle className="h-4 w-4 text-blue-600" />}
          statusVariant="info"
        />
        <StatCard
          title="Prevented Loss (24h)"
          value="₹ 42.5 Lakh"
          change="Prevented Fraud"
          changeType="increase"
          comparisonText="Automatic Enforcement"
          icon={<ShieldCheck className="h-4 w-4 text-purple-600" />}
          statusVariant="success"
        />
      </div>

      {/* Monte Carlo Simulator */}
      <MonteCarloBacktester />

      {/* Rules Data Table */}
      <Card>
        <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <CardTitle>Fraud Rule Policy Directory</CardTitle>
            <CardDescription>Hot-reloaded rule weights evaluated on incoming UPI payments</CardDescription>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <div className="w-64">
              <Input
                placeholder="Search Rule Code or Name..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                leftIcon={<Search className="h-4 w-4 text-slate-400" />}
              />
            </div>
            <div className="w-40">
              <Select
                value={categoryFilter}
                onChange={(e) => setCategoryFilter(e.target.value)}
                options={[
                  { label: 'All Categories', value: 'ALL' },
                  { label: 'Velocity', value: 'VELOCITY' },
                  { label: 'Device', value: 'DEVICE' },
                  { label: 'Geofence', value: 'GEOFENCE' },
                  { label: 'Identity', value: 'IDENTITY' },
                ]}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <DataTable data={filteredRules} columns={columns} pageSize={10} />
        </CardContent>
      </Card>

      {/* Visual Rule Builder Drawer */}
      <Drawer isOpen={isEditorOpen} onClose={() => setIsEditorOpen(false)} title="No-Code Visual Rule Builder">
        <form onSubmit={handleSaveRule} className="space-y-4 text-xs">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="font-semibold text-slate-700 dark:text-slate-300 block mb-1">Rule Code</label>
              <Input
                value={editingRule.code}
                onChange={(e) => setEditingRule({ ...editingRule, code: e.target.value })}
                required
              />
            </div>
            <div>
              <label className="font-semibold text-slate-700 dark:text-slate-300 block mb-1">Deployment Mode</label>
              <Select
                value={editingRule.status}
                onChange={(e: any) => setEditingRule({ ...editingRule, status: e.target.value })}
                options={[
                  { label: 'Shadow Mode (Silent Testing)', value: 'SHADOW_MODE' },
                  { label: 'Active Live Enforcement', value: 'ACTIVE' },
                ]}
              />
            </div>
          </div>

          <div>
            <label className="font-semibold text-slate-700 dark:text-slate-300 block mb-1">Rule Name</label>
            <Input
              value={editingRule.name}
              onChange={(e) => setEditingRule({ ...editingRule, name: e.target.value })}
              required
            />
          </div>

          <div>
            <label className="font-semibold text-slate-700 dark:text-slate-300 block mb-1">
              IF Condition Expression (CEL Syntax)
            </label>
            <textarea
              value={editingRule.condition}
              onChange={(e) => setEditingRule({ ...editingRule, condition: e.target.value })}
              className="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-slate-900 text-emerald-400 font-mono p-3 text-xs focus:ring-2 focus:ring-indigo-500"
              rows={4}
            />
          </div>

          <div className="pt-4 flex justify-end gap-2 border-t border-slate-200 dark:border-slate-800">
            <Button variant="outline" size="sm" onClick={() => setIsEditorOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="sm">
              Save Rule
            </Button>
          </div>
        </form>
      </Drawer>
    </div>
  );
}
