'use client';

import React from 'react';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { ShieldCheck, FileText } from 'lucide-react';

export default function TermsPage() {
  return (
    <div className="min-h-screen flex flex-col bg-[#080c14] text-slate-100 font-sans">
      <PublicHeader />

      <main className="flex-1 max-w-4xl mx-auto px-4 py-16 space-y-8">
        <div className="space-y-3">
          <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-950/80 border border-blue-500/30 px-3 py-1 text-xs font-mono text-blue-400 font-semibold">
            <ShieldCheck className="h-3.5 w-3.5" /> Legal Framework
          </span>
          <h1 className="text-3xl font-extrabold tracking-tight text-white font-mono">
            SentinelX Master Enterprise Terms of Service
          </h1>
          <p className="text-slate-400 text-sm">
            Effective Date: September 13, 2026 • Enterprise SaaS Service Level Agreement (SLA)
          </p>
        </div>

        <div className="p-6 rounded-2xl bg-slate-900/60 border border-slate-800 text-xs text-slate-300 space-y-4 leading-relaxed">
          <h2 className="text-base font-bold text-white">1. Sub-10ms Scoring SLA & Availability</h2>
          <p>
            SentinelX guarantees 99.99% uptime for the real-time payment risk scoring engine with a sub-10ms median scoring latency SLA across API Gateway endpoints.
          </p>
        </div>
      </main>

      <PublicFooter />
    </div>
  );
}
