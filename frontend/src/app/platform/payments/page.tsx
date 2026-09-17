'use client';

import React from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { Server, CreditCard, ArrowRight, CheckCircle2, RefreshCw } from 'lucide-react';

export default function PaymentsPage() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans">
      <PublicHeader />

      <main className="flex-1 py-16">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <Badge variant="info" size="sm" className="mb-3 uppercase tracking-wider font-mono">
              Payments & Settlement Mesh
            </Badge>
            <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
              UPI Payment Mesh Infrastructure
            </h1>
            <p className="mt-4 text-base text-slate-600 leading-relaxed">
              High-availability payment routing across NPCI gateway, core bank switches, merchant QR generators, and instant settlement.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16">
            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center mb-3">
                  <CreditCard className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Instant UPI Gateway</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Real-time debit and credit settlement with automated retry logic and offline transaction sync.
                </CardDescription>
              </CardHeader>
            </Card>

            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-emerald-100 text-emerald-600 flex items-center justify-center mb-3">
                  <RefreshCw className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Reconciliation Engine</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  3-way reconciliation between NPCI clearing logs, core bank ledgers, and internal payment records.
                </CardDescription>
              </CardHeader>
            </Card>

            <Card className="border border-slate-200 shadow-sm">
              <CardHeader>
                <div className="h-10 w-10 rounded-lg bg-purple-100 text-purple-600 flex items-center justify-center mb-3">
                  <Server className="h-5 w-5" />
                </div>
                <CardTitle className="text-lg font-bold">Merchant Settlements</CardTitle>
                <CardDescription className="text-xs leading-relaxed">
                  Batch settlement processing with automatic MDR calculation and PDF invoice generation.
                </CardDescription>
              </CardHeader>
            </Card>
          </div>

          <div className="p-8 rounded-2xl bg-slate-900 text-white text-center">
            <h2 className="text-2xl font-bold">Monitor Live Transactions & Settlements</h2>
            <p className="text-xs text-slate-400 mt-2 mb-6">
              View real-time payment streams, merchant ledgers, and settlement batch status.
            </p>
            <Link href="/transactions">
              <Button variant="primary" size="md" rightIcon={<ArrowRight className="h-4 w-4" />}>
                Go to Transactions Console
              </Button>
            </Link>
          </div>
        </div>
      </main>

      <PublicFooter />
    </div>
  );
}
