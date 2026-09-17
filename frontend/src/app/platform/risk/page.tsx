'use client';

import React from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Zap, ShieldCheck, ArrowRight, CheckCircle2, Sliders, Activity } from 'lucide-react';

export default function RiskEnginePage() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans">
      <PublicHeader />

      <main className="flex-1 py-16">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <Badge variant="info" size="sm" className="mb-3 uppercase tracking-wider font-mono">
              Risk Engine
            </Badge>
            <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
              Sub-10ms Real-Time Scoring Engine
            </h1>
            <p className="mt-4 text-base text-slate-600 leading-relaxed">
              Every UPI transaction evaluated in real time against composite velocity vectors, geolocation anomalies, and user risk tier history.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16">
            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-amber-100 text-amber-600 flex items-center justify-center mb-3">
                  <Zap className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Velocity Vectors</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Monitors transaction count and sum over 1m, 5m, 1h, and 24h rolling windows per user and UPI handle.
                </CardDescription>
              </CardHeader>
            </Card>

            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center mb-3">
                  <Sliders className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Dynamic Rule Engine</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Configure custom threshold rules with instant hot-reloading across the gateway cluster.
                </CardDescription>
              </CardHeader>
            </Card>

            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-purple-100 text-purple-600 flex items-center justify-center mb-3">
                  <Activity className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Risk Tiers (0-1000)</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Categorizes accounts into Low (0-200), Medium (201-600), High (601-850), and Critical (851-1000) tiers.
                </CardDescription>
              </CardHeader>
            </Card>
          </div>

          <div className="p-8 rounded-2xl bg-slate-900 text-white text-center">
            <h2 className="text-2xl font-bold">Inspect Risk Rules & User Profiles</h2>
            <p className="text-xs text-slate-400 mt-2 mb-6">
              View active risk scoring profiles, user trajectory graphs, and rule engine configs.
            </p>
            <Link href="/risk">
              <Button variant="primary" size="md" rightIcon={<ArrowRight className="h-4 w-4" />}>
                Go to Risk Operations
              </Button>
            </Link>
          </div>
        </div>
      </main>

      <PublicFooter />
    </div>
  );
}
