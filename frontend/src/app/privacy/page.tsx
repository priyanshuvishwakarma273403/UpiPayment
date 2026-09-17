'use client';

import React from 'react';
import Link from 'next/link';
import { PublicHeader } from '@/components/layout/PublicHeader';
import { PublicFooter } from '@/components/layout/PublicFooter';
import { ShieldCheck, Lock, FileText, CheckCircle2 } from 'lucide-react';

export default function PrivacyPage() {
  return (
    <div className="min-h-screen flex flex-col bg-[#080c14] text-slate-100 font-sans">
      <PublicHeader />

      <main className="flex-1 max-w-4xl mx-auto px-4 py-16 space-y-8">
        <div className="space-y-3">
          <Badge text="Enterprise Security & Privacy" />
          <h1 className="text-3xl font-extrabold tracking-tight text-white font-mono">
            SentinelX Privacy Policy & Data Governance
          </h1>
          <p className="text-slate-400 text-sm">
            Last Updated: September 13, 2026 • Authoritative Security Architecture Statement
          </p>
        </div>

        <div className="prose prose-invert max-w-none text-slate-300 text-xs leading-relaxed space-y-6">
          <section className="p-6 rounded-2xl bg-slate-900/60 border border-slate-800 space-y-3">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <Lock className="h-4 w-4 text-blue-500" /> 1. Data Encryption & Storage Standards
            </h2>
            <p>
              All customer transaction telemetry, device fingerprints, and KYC identity verification records ingested by SentinelX are encrypted in transit using TLS 1.3 and at rest using AES-256-GCM. Cryptographic SHA-256 evidence hashing is enforced across all audit logs.
            </p>
          </section>

          <section className="p-6 rounded-2xl bg-slate-900/60 border border-slate-800 space-y-3">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <ShieldCheck className="h-4 w-4 text-emerald-500" /> 2. Regulatory Compliance & FIU Guidelines
            </h2>
            <p>
              SentinelX complies fully with global anti-money laundering (AML) and counter-terrorist financing (CTF) frameworks, including Financial Intelligence Unit (FIU-IND) Suspicious Transaction Reporting regulations.
            </p>
          </section>
        </div>
      </main>

      <PublicFooter />
    </div>
  );
}

function Badge({ text }: { text: string }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-950/80 border border-blue-500/30 px-3 py-1 text-xs font-mono text-blue-400 font-semibold">
      <ShieldCheck className="h-3.5 w-3.5" />
      {text}
    </span>
  );
}
