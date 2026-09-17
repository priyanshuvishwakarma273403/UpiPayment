'use client';

import React, { useState } from 'react';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Input } from '@/components/ui/Input';
import { Store, ShieldCheck, Sliders, DollarSign, FileText, CheckCircle2 } from 'lucide-react';
import { useToast } from '@/lib/useToast';

export default function MerchantPortalPage() {
  const { toast } = useToast();
  const [threshold, setThreshold] = useState(70);

  const saveMerchantConfig = (e: React.FormEvent) => {
    e.preventDefault();
    toast('Merchant Custom Risk Threshold Saved', `Updated automated block score threshold to ${threshold}/100.`, 'success');
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Multi-Tenant Merchant Fraud Self-Service Portal"
        description="Dedicated merchant portal to set custom risk tolerances, contest chargeback disputes, and view fraud loss metrics."
        breadcrumbs={['SentinelX', 'Merchant Operations', 'Portal']}
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Custom Threshold Settings & Chargebacks */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-bold">Custom Risk Tolerance & Automated Actions</CardTitle>
              <CardDescription>Configure threshold scores for automatic block vs manual review</CardDescription>
            </CardHeader>

            <CardContent>
              <form onSubmit={saveMerchantConfig} className="space-y-4 text-xs">
                <div>
                  <div className="flex justify-between font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    <span>Auto-Block Score Threshold</span>
                    <span className="font-mono font-bold text-indigo-600 dark:text-indigo-400">{threshold} / 100</span>
                  </div>
                  <input
                    type="range"
                    min="30"
                    max="95"
                    value={threshold}
                    onChange={(e) => setThreshold(Number(e.target.value))}
                    className="w-full h-2 bg-slate-200 dark:bg-slate-700 rounded-lg appearance-none cursor-pointeraccent-indigo-600"
                  />
                </div>

                <div className="p-3 rounded-lg bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-[11px] text-slate-600 dark:text-slate-400 leading-relaxed">
                  Transactions with a risk score above <strong>{threshold}</strong> will be automatically rejected before checkout completion.
                </div>

                <Button type="submit" variant="primary" size="sm">
                  Save Merchant Configuration
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>

        {/* Right 1 Col: Merchant Revenue Loss Prevented */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-sm font-bold">Prevented Loss Metrics</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-xs font-mono">
              <div className="p-3 rounded-lg bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-900/60 text-emerald-900 dark:text-emerald-300">
                <span className="text-[10px] uppercase block">Saved Revenue (30d)</span>
                <span className="text-xl font-bold font-mono">₹ 18,40,000</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
