'use client';

import React from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card';
import { ShieldCheck, Cpu, Database, Server, Layers, ArrowRight, Zap, CheckCircle2, RefreshCw } from 'lucide-react';

export default function PlatformOverviewPage() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans">
      <PublicHeader />

      <main className="flex-1 py-16">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="text-center max-w-3xl mx-auto mb-12">
            <Badge variant="info" size="sm" className="mb-3 uppercase tracking-wider font-mono">
              SentinelX Platform Architecture
            </Badge>
            <h1 className="text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
              27 Microservices. One Intelligence Mesh.
            </h1>
            <p className="mt-4 text-base text-slate-600 leading-relaxed">
              SentinelX connects core banking gateways, real-time transaction processing, machine learning inference, and graph network analytics in a unified high-performance platform.
            </p>
          </div>

          {/* Hero Visual Artwork Banner */}
          <div className="mb-16 rounded-2xl overflow-hidden border border-slate-200 shadow-xl bg-white group">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src="/hero-side-illustration.jpg"
              alt="SentinelX Real-Time Transaction Intelligence Ecosystem"
              className="w-full h-auto object-cover rounded-2xl transition-transform duration-500 group-hover:scale-101"
            />
          </div>

          {/* Microservices Matrix */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16">
            {[
              {
                title: 'Transaction & Payment Mesh',
                desc: 'Spring Boot Payment, Transaction, NPCI Gateway, Settlement, Reconciliation, and Wallet microservices handling instant payments.',
                services: ['PaymentService', 'TransactionService', 'NpciService', 'SettlementService', 'ReconciliationService', 'WalletService'],
                icon: <Server className="h-5 w-5 text-blue-600" />
              },
              {
                title: 'Risk & Fraud Intelligence',
                desc: 'Sub-10ms risk scoring, SHAP feature explanation, continuous model retraining, and graph relationship clustering.',
                services: ['RiskService', 'FraudService', 'MlService (FastAPI)', 'AiService', 'McpController'],
                icon: <Cpu className="h-5 w-5 text-indigo-600" />
              },
              {
                title: 'Compliance & Identity',
                desc: 'Auth RBAC authorization, Aadhaar/PAN KYC verification, OFAC/PEP AML screening, and Dispute chargeback management.',
                services: ['AuthService', 'KycService', 'AmlService', 'DisputeService', 'MerchantService'],
                icon: <ShieldCheck className="h-5 w-5 text-emerald-600" />
              }
            ].map((col) => (
              <Card key={col.title} className="border border-slate-200 shadow-sm">
                <CardHeader>
                  <div className="h-10 w-10 rounded-lg bg-slate-100 flex items-center justify-center mb-3">
                    {col.icon}
                  </div>
                  <CardTitle className="text-lg font-bold">{col.title}</CardTitle>
                  <CardDescription className="text-xs leading-relaxed">{col.desc}</CardDescription>
                </CardHeader>
                <CardContent className="pt-0">
                  <div className="text-xs font-semibold text-slate-700 mb-2">Microservices Included:</div>
                  <div className="flex flex-wrap gap-1.5">
                    {col.services.map((s) => (
                      <span key={s} className="font-mono text-[10px] bg-slate-100 border border-slate-200 rounded px-2 py-0.5 text-slate-700">
                        {s}
                      </span>
                    ))}
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {/* CTA */}
          <div className="p-8 rounded-2xl bg-slate-900 text-white text-center flex flex-col items-center justify-center space-y-4">
            <h2 className="text-2xl font-bold">Inspect Backend Contract Specification</h2>
            <p className="text-xs text-slate-400 max-w-xl">
              Every API contract is mapped directly to Spring Boot controllers and Python FastAPI schemas.
            </p>
            <div className="flex gap-4">
              <Link href="/dashboard">
                <Button variant="primary" size="md" rightIcon={<ArrowRight className="h-4 w-4" />}>
                  Explore Analyst Console
                </Button>
              </Link>
              <Link href="/developers">
                <Button variant="outline" size="md" className="border-slate-700 text-slate-300 hover:bg-slate-800">
                  Developer Documentation
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </main>

      <PublicFooter />
    </div>
  );
}
